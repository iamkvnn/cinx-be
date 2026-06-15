import json
import logging
from sqlalchemy.orm import Session
from sqlalchemy import select
from sentence_transformers import SentenceTransformer
from app.core.config import settings
from app.entities.course import Course
from app.services.llm_client import DigitalOceanLLMClient
from app.services.rag_index import IndexedChunk, rag_index, rebuild_rag_index

# Load the model globally so it doesn't reload on every request
embed_model = SentenceTransformer("all-MiniLM-L6-v2")
logger = logging.getLogger(__name__)

class RAGService:
    def __init__(self, db: Session):
        self.db = db
        self.llm = DigitalOceanLLMClient()

    def embed_text(self, text: str) -> list[float]:
        return embed_model.encode([text])[0].tolist()

    def generate_learning_path(self, user_goal: str, top_k: int = 5):
        goal_embedding = self.embed_text(user_goal)

        top_k = max(1, top_k)
        chunk_results = rag_index.search(goal_embedding, top_k)
        if not chunk_results:
            rebuild_rag_index(self.db)
            chunk_results = rag_index.search(goal_embedding, top_k)
        
        if not chunk_results:
            return {"error": "No course chunks available in database."}

        course_scores: dict[str, float] = {}
        course_chunks: dict[str, list[IndexedChunk]] = {}
        for chunk, score in chunk_results:
            course_scores[chunk.course_id] = max(score, course_scores.get(chunk.course_id, float("-inf")))
            course_chunks.setdefault(chunk.course_id, []).append(chunk)

        top_course_ids = [
            course_id
            for course_id, _ in sorted(course_scores.items(), key=lambda item: item[1], reverse=True)[:5]
        ]

        # Fetch the FULL structures of these specific courses
        courses = self.db.execute(select(Course).where(Course.id.in_(top_course_ids))).scalars().all()
        course_by_id = {course.id: course for course in courses}
        ordered_courses = [course_by_id[course_id] for course_id in top_course_ids if course_id in course_by_id]

        # Build context utilizing full course hierarchy so AI won't miss any lessons!
        context_parts = []
        for c in ordered_courses:
            selected_lesson_ids = {
                lesson_id
                for chunk in course_chunks.get(c.id, [])
                for lesson_id in (chunk.lesson_ids or [])
            }
            course_text = f"Course ID: {c.id}, Title: {c.title}\nRelevant Sections:\n"
            sections = c.curriculum or []
            if sections:
                for sec in sections:
                    lessons = sec.get("lessons", [])
                    relevant_lessons = [
                        lesson
                        for lesson in lessons
                        if not selected_lesson_ids or lesson.get("id") in selected_lesson_ids
                    ]
                    if not relevant_lessons:
                        continue
                    course_text += f"  - Section Title: {sec.get('title')}\n"
                    for les in relevant_lessons:
                        course_text += f"      * Lesson ID: {les.get('id')}, Title: {les.get('title')}, Type: {les.get('lessonType')}\n"
            context_parts.append(course_text)

        context_text = "\n\n".join(context_parts)
        
        if not self.llm.is_configured():
            return {
                "error": "DigitalOcean model access key not configured. Cannot generate learning path.",
                "retrieved_chunks": [{"course_id": c.id} for c in ordered_courses]
            }
            
        prompt = f"""
You are an AI learning path generator.

Goal: "{user_goal}"

Use ONLY the Retrieved Content below to create a logical learning path.

Rules:

* Select 1 to 3 most relevant courses only.
* Exclude weakly related courses.
* Return selected course IDs in `courseIds`.
* Build `lessonOrder` as one ordered list of lesson IDs.
* Lessons may be reordered across courses/sections for the best beginner-to-advanced flow.
* Include important foundational and sequential lessons.
* Place quizzes/assignments after related lessons.
* Do not invent IDs, titles, courses, lessons, or extra content.
* Every lesson ID must belong to one selected course.
* No duplicate lesson IDs.
* Output valid JSON only. No markdown. No trailing commas.

Retrieved Content:
{context_text}

JSON format:
{{
"pathName": "string",
"description": "string",
"detailReason": "string",
"courseIds": ["string"],
"lessonOrder": ["string"]
}}
"""

        logger.info(
            "Generating learning path with DigitalOcean LLM model=%s course_count=%s chunk_count=%s",
            settings.DIGITALOCEAN_LLM_MODEL,
            len(ordered_courses),
            len(chunk_results),
        )

        text, error = self.llm.generate_learning_path_json(prompt)
        if error:
            return {
                "error": error,
                "retrieved_chunks": [{"course_id": c.id} for c in ordered_courses],
            }
        
        try:
            result = json.loads(self._extract_json(text))
            return result
        except json.JSONDecodeError:
            return {"error": "Failed to parse LLM response into JSON", "raw": text}

    def _extract_json(self, text: str) -> str:
        cleaned = text.replace("```json", "").replace("```", "").strip()
        if cleaned.startswith("{") and cleaned.endswith("}"):
            return cleaned

        start = cleaned.find("{")
        end = cleaned.rfind("}")
        if start != -1 and end != -1 and end > start:
            return cleaned[start:end + 1]
        return cleaned
