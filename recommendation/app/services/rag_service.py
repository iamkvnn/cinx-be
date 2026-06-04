import json
import numpy as np
from sqlalchemy.orm import Session
from sqlalchemy import select
from sentence_transformers import SentenceTransformer
from google import genai
from app.core.config import settings
from app.entities.course_chunk import CourseChunk
from app.entities.course import Course

# Load the model globally so it doesn't reload on every request
embed_model = SentenceTransformer("all-MiniLM-L6-v2")

class RAGService:
    def __init__(self, db: Session):
        self.db = db
        if settings.GEMINI_API_KEY:
            self.llm = genai.Client(api_key=settings.GEMINI_API_KEY)
        else:
            self.llm = None

    def embed_text(self, text: str) -> list[float]:
        return embed_model.encode([text])[0].tolist()

    def generate_learning_path(self, user_goal: str, top_k: int = 5):
        goal_embedding = np.array(self.embed_text(user_goal))
        
        # Load all chunks and compute cosine similarity
        chunks = self.db.execute(select(CourseChunk)).scalars().all()
        
        if not chunks:
            return {"error": "No course chunks available in database."}

        # Vector search
        chunk_embeddings = np.array([c.embedding for c in chunks])
        
        # Cosine similarity
        norm_goal = np.linalg.norm(goal_embedding)
        norm_chunks = np.linalg.norm(chunk_embeddings, axis=1)
        
        # Avoid division by zero
        norm_chunks[norm_chunks == 0] = 1e-9
        if norm_goal == 0: norm_goal = 1e-9
            
        similarities = np.dot(chunk_embeddings, goal_embedding) / (norm_chunks * norm_goal)

        # Sort all chunks by similarity in descending order
        sorted_indices = np.argsort(similarities)[::-1]
        top_course_ids = [chunks[i].course_id for i in sorted_indices[:top_k]]

        # Fetch the FULL structures of these specific courses
        courses = self.db.execute(select(Course).where(Course.id.in_(top_course_ids))).scalars().all()

        # Build context utilizing full course hierarchy so AI won't miss any lessons!
        context_parts = []
        for c in courses:
            course_text = f"Course ID: {c.id}, Title: {c.title}\nDescription: {c.description}\nSections:\n"
            sections = c.curriculum or []
            if sections:
                for sec in sections:
                    course_text += f"  - Section Title: {sec.get('title')}\n"
                    for les in sec.get('lessons', []):
                        course_text += f"      * Lesson ID: {les.get('id')}, Title: {les.get('title')}, Type: {les.get('lessonType')}\n"
            context_parts.append(course_text)

        context_text = "\n\n".join(context_parts)
        
        if not self.llm:
            return {
                "error": "Gemini API key not configured. Cannot generate learning path.",
                "retrieved_chunks": [{"course_id": c.id} for c in courses]
            }
            
        prompt = f"""
You are an expert AI learning path generator. The user has a learning goal: "{user_goal}".
Below is the curriculum of highly relevant courses. Your task is to construct a comprehensive, logically sequenced learning path.

CRITICAL REQUIREMENTS:
1. SELECT THE BEST COURSES: Review the provided courses and select AT LEAST 2 but NO MORE THAN 5 of the most relevant courses to form the learning path. Discard any courses that are only weakly related to the user's goal.
2. INCLUDE ALL RELEVANT LESSONS: For the courses you select, do not skip important foundational or sequential lessons. Ensure the path is complete.
3. LOGICAL REORDERING: You MUST structure the learning flow logically from beginner to advanced. You are allowed and encouraged to reorder lessons, even mixing lessons from different sections or courses if it creates a more optimal step-by-step learning experience.
4. NO HALLUCINATION: Only use the exact `Course ID` and `Lesson ID` provided in the Retrieved Content.

Retrieved Content:
{context_text}

Output ONLY valid JSON in the following format, with no markdown code blocks around it:
{{
  "pathName": "Generated Path Name",
  "description": "Detailed description of why this path fits the goal and how the sequence flows",
  "items": [
    {{
      "courseId": "string",
      "lessonId": "string",
      "reason": "Why this specific lesson is placed at this point in the sequence"
    }}
  ]
}}
"""
        print("LLM Prompt:", prompt)  # Debug: Print the prompt being sent to the LLM
        response = self.llm.models.generate_content(
                model="gemini-3-flash-preview",
                contents=prompt
        )
        text = response.text.replace("```json", "").replace("```", "").strip()
        print("LLM Raw Response:", text)  # Debug: Print the raw response from the LLM
        
        try:
            result = json.loads(text)
            return result
        except json.JSONDecodeError:
            return {"error": "Failed to parse LLM response into JSON", "raw": text}
