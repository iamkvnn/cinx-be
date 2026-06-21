import logging
import uuid
from datetime import datetime
from typing import Any

from sqlalchemy.orm import Session

from app.agent.schemas import LearningPathProposalResponse, LearningPathProposalItem, ProposalUpdateRequest
from app.agent.services.learning_commit_client import LearningPathCommitClient
from app.entities.course import Course
from app.entities.agent_session import AgentSession
from app.repositories.course_repository import CourseRepository
from app.services.embedding_model import embed_query
from app.services.rag_index import IndexedChunk, rag_index, rebuild_rag_index
from app.services.llm_client import DigitalOceanLLMClient

logger = logging.getLogger(__name__)



class LearningPathContextService:
    def __init__(self, db: Session):
        self.db = db
        self.course_repo = CourseRepository(db)

    def retrieve_context(
        self,
        goal: str,
        top_k: int = 5,
        context_course_ids: list[str] | None = None,
    ) -> dict[str, Any]:
        top_k = max(1, min(int(top_k or 5), 5))
        context_course_ids = _dedupe([str(course_id) for course_id in (context_course_ids or []) if course_id])[:5]
        query_embedding = embed_query(goal)
        chunk_results = rag_index.search(query_embedding, max(20, top_k * 4), source_type="course")
        if not chunk_results and not context_course_ids:
            rebuild_rag_index(self.db)
            chunk_results = rag_index.search(query_embedding, max(20, top_k * 4), source_type="course")
        if not chunk_results and not context_course_ids:
            return {"candidateCourses": [], "relevantLessons": [], "citations": []}

        course_scores: dict[str, float] = {}
        course_chunks: dict[str, list[tuple[IndexedChunk, float]]] = {}
        for chunk, score in chunk_results:
            if not chunk.course_id:
                continue
            if context_course_ids and chunk.course_id not in context_course_ids:
                continue
            course_scores[chunk.course_id] = max(course_scores.get(chunk.course_id, float("-inf")), score)
            course_chunks.setdefault(chunk.course_id, []).append((chunk, score))

        if context_course_ids:
            ordered_course_ids = context_course_ids[:top_k]
        else:
            ordered_course_ids = [
                course_id
                for course_id, _ in sorted(course_scores.items(), key=lambda item: item[1], reverse=True)[:top_k]
            ]
        courses = _order_courses(self.course_repo.get_by_ids(ordered_course_ids), ordered_course_ids)

        candidate_courses = []
        relevant_lessons = []
        citations = []
        for index, course in enumerate(courses):
            fallback_score = max(0.0, 1.0 - (index * 0.01)) if context_course_ids else 0.0
            score = round(course_scores.get(course.id, fallback_score), 4)
            candidate_courses.append(
                {
                    "courseId": course.id,
                    "title": course.title,
                    "description": _short_text(course.description, 400),
                    "categoryName": course.category_name,
                    "rating": course.rating,
                    "price": course.price,
                    "discountedPrice": course.discounted_price,
                    "duration": course.duration,
                    "hasCertificate": course.has_certificate,
                    "score": score,
                    "url": f"https://shiny.id.vn/courses/{course.id}",
                }
            )
            citations.append({
                "sourceType": "course",
                "title": course.title,
                "courseId": course.id,
                "sourceUrl": f"https://shiny.id.vn/courses/{course.id}",
                "score": score
            })

            selected_lesson_ids = {
                lesson_id
                for chunk, _ in course_chunks.get(course.id, [])
                for lesson_id in (chunk.lesson_ids or [])
            }
            lessons = _course_lessons(course)
            if selected_lesson_ids:
                lessons = [lesson for lesson in lessons if lesson["lessonId"] in selected_lesson_ids]
            for lesson in lessons[:5]:
                relevant_lessons.append(lesson)

        return {
            "goal": goal,
            "contextCourseIds": context_course_ids,
            "candidateCourses": candidate_courses,
            "relevantLessons": relevant_lessons,
            "candidateCourseIds": [course["courseId"] for course in candidate_courses],
            "citations": citations,
        }


class LearningPathProposalService:
    def __init__(self, db: Session, llm_client: DigitalOceanLLMClient | None = None):
        self.db = db
        self.course_repo = CourseRepository(db)
        self.commit_client = LearningPathCommitClient()
        self.llm = llm_client or DigitalOceanLLMClient()


    def get_proposal(self, session_id: str) -> LearningPathProposalResponse:
        proposal = self._active_proposal(session_id)
        return self._to_response(proposal)

    def save_proposal_from_llm(
        self,
        session_id: str,
        proposal_data: dict[str, Any],
        context_output: dict[str, Any] | None = None,
    ) -> LearningPathProposalResponse:
        allowed_lessons = {}
        candidate_course_ids = []
        if context_output:
            allowed_lessons = _lesson_map(context_output.get("relevantLessons") or [])
            candidate_course_ids = list(context_output.get("candidateCourseIds") or [])

        raw_items = proposal_data.get("items") or []
        items = []
        if context_output:
            items = self._validated_items(raw_items, allowed_lessons)

        # Fallback/Direct validation: Query the database for any lesson not found in context_output
        if not items:
            seen_lessons = set()
            for raw in raw_items:
                lesson_id = str(raw.get("lessonId") or "").strip()
                course_id = str(raw.get("courseId") or "").strip()
                if not lesson_id or not course_id or lesson_id in seen_lessons:
                    continue
                # Fetch course from DB
                course = self.db.get(Course, course_id)
                if not course or course.status != "PUBLISHED":
                    continue
                # Search for the lesson in the curriculum
                lessons = _course_lessons(course)
                lesson = next((l for l in lessons if l["lessonId"] == lesson_id), None)
                if lesson:
                    seen_lessons.add(lesson_id)
                    items.append({
                        "courseId": course_id,
                        "courseTitle": course.title,
                        "courseUrl": f"https://shiny.id.vn/courses/{course_id}",
                        "lessonId": lesson_id,
                        "lessonTitle": lesson.get("lessonTitle"),
                        "orderIndex": len(items),
                        "isSuggested": bool(raw.get("isSuggested", True)),
                    })
                    if course_id not in candidate_course_ids:
                        candidate_course_ids.append(course_id)

        if not items:
            raise ValueError("Learning path proposal does not contain any valid lessons.")

        proposal = {
            "proposalId": str(uuid.uuid4()),
            "sessionId": session_id,
            "version": 1,
            "title": str(proposal_data.get("title") or "Suggested learning path").strip(),
            "description": proposal_data.get("description"),
            "courseIds": _dedupe([item["courseId"] for item in items]),
            "candidateCourseIds": candidate_course_ids,
            "items": items,
            "status": "PROPOSED",
            "createdAt": datetime.utcnow().isoformat(),
            "updatedAt": datetime.utcnow().isoformat(),
        }
        state = self._state(session_id)
        state["activeProposal"] = proposal
        state["lastCourseIds"] = proposal["courseIds"]
        state["awaitingConfirmation"] = True
        self._save_state(session_id, state)
        return self._to_response(proposal)

    def update_proposal(self, session_id: str, request: ProposalUpdateRequest) -> LearningPathProposalResponse:
        state = self._state(session_id)
        proposal = dict(state.get("activeProposal") or {})
        if not proposal:
            raise ValueError("No active learning path proposal.")
        if request.version is not None and int(request.version) != int(proposal.get("version") or 0):
            raise ValueError("Learning path proposal has changed. Refresh before editing.")

        items = list(proposal.get("items") or [])
        context_output = None

        if request.operation == "UPDATE_METADATA":
            if request.title:
                proposal["title"] = request.title.strip()
            if request.description:
                proposal["description"] = request.description.strip()

        elif request.operation == "ADD_COURSE":
            # Support both courseId and courseIds
            target_course_ids = []
            if request.courseIds:
                target_course_ids.extend(request.courseIds)
            if request.courseId:
                target_course_ids.append(request.courseId)

            # If no course IDs are provided, try query
            if not target_course_ids and request.query:
                query = str(request.query).strip()
                context_output = LearningPathContextService(self.db).retrieve_context(query, top_k=5)
                candidates = [
                    course
                    for course in context_output.get("candidateCourses") or []
                    if isinstance(course, dict) and course.get("courseId")
                ]
                existing_course_ids = {item.get("courseId") for item in items}
                selected = next((course for course in candidates if course.get("courseId") not in existing_course_ids), None)
                selected = selected or (candidates[0] if candidates else None)
                if selected:
                    target_course_ids.append(selected["courseId"])

            if not target_course_ids:
                raise ValueError("No matching courses found to add.")

            target_course_ids = _dedupe(target_course_ids)

            # Add lessons for each course
            lessons_added = False
            for c_id in target_course_ids:
                existing_course_ids = {item.get("courseId") for item in items}
                if c_id in existing_course_ids:
                    continue
                course_lessons = self._lessons_for_course(c_id, context_output or state.get("lastLearningPathContext"))
                additions = self._new_items_from_lessons(course_lessons[:3], items)
                if additions:
                    items.extend(additions)
                    lessons_added = True

            if not lessons_added and not request.courseIds:
                raise ValueError("Course is already in the proposal or has no valid lessons.")

            if context_output:
                state["lastLearningPathContext"] = context_output
                proposal["candidateCourseIds"] = _dedupe(
                    list(proposal.get("candidateCourseIds") or []) + list(context_output.get("candidateCourseIds") or [])
                )

        elif request.operation == "ADD_LESSON":
            items, context_output = self._add_lesson_items(items, request, state)
            if context_output:
                state["lastLearningPathContext"] = context_output
                proposal["candidateCourseIds"] = _dedupe(
                    list(proposal.get("candidateCourseIds") or []) + list(context_output.get("candidateCourseIds") or [])
                )

        elif request.operation == "REMOVE_LESSON":
            remove_ids = set()
            if request.lessonIds:
                remove_ids.update(request.lessonIds)
            if request.lessonId:
                remove_ids.add(request.lessonId)
            if not remove_ids:
                raise ValueError("lessonId or lessonIds is required for REMOVE_LESSON.")
            items = [item for item in items if item.get("lessonId") not in remove_ids]

        elif request.operation == "REMOVE_COURSE":
            remove_course_ids = set()
            if request.courseIds:
                remove_course_ids.update(request.courseIds)
            if request.courseId:
                remove_course_ids.add(request.courseId)
            if not remove_course_ids:
                raise ValueError("courseId or courseIds is required for REMOVE_COURSE.")
            items = [item for item in items if item.get("courseId") not in remove_course_ids]

        elif request.operation == "MOVE_LESSON":
            items = self._move_item(items, request.lessonId, request.orderIndex)

        # Allow user to update title/description alongside other operations
        if request.title:
            proposal["title"] = request.title.strip()
        if request.description:
            proposal["description"] = request.description.strip()

        if not items:
            raise ValueError("Learning path proposal must contain at least one item.")
        proposal["items"] = self._normalize_order(items)
        proposal["courseIds"] = _dedupe([item["courseId"] for item in proposal["items"]])
        proposal["version"] = int(proposal.get("version") or 1) + 1
        proposal["updatedAt"] = datetime.utcnow().isoformat()

        # Re-generate metadata (title and description) based on updated items (if not explicitly provided by user)
        if not request.title and not request.description and self.llm and self.llm.is_configured():
            try:
                items_desc = []
                for item in proposal["items"]:
                    course_title = item.get("courseTitle") or ""
                    lesson_title = item.get("lessonTitle") or ""
                    items_desc.append(f"- Course: {course_title}, Lesson: {lesson_title}")
                items_str = "\n".join(items_desc)

                prompt = f"""
You are a learning path metadata generator.
Based on the following updated learning path items, generate an appropriate title and brief description (1-2 sentences) for the learning path.
Maintain the same language as the current title and description if possible.

Current Title: {proposal.get("title")}
Current Description: {proposal.get("description")}

Updated Learning Path Items:
{items_str}

Return strict JSON only (no preamble, no markdown fences):
{{
  "title": "A concise title",
  "description": "A brief description"
}}
"""
                result = self.llm.generate_json_required(
                    prompt,
                    system="You are a learning path metadata generator. Return strict JSON only.",
                    temperature=0.0,
                    label="learning_path_metadata_update",
                )
                if result and isinstance(result, dict):
                    proposal["title"] = result.get("title") or proposal["title"]
                    proposal["description"] = result.get("description") or proposal.get("description")
            except Exception as e:
                logger.warning(f"Failed to update metadata using LLM: {e}")

        state["activeProposal"] = proposal
        state["lastCourseIds"] = proposal["courseIds"]
        state["awaitingConfirmation"] = True
        self._save_state(session_id, state)
        return self._to_response(proposal)

    def _add_lesson_items(
        self,
        items: list[dict[str, Any]],
        request: ProposalUpdateRequest,
        state: dict[str, Any],
    ) -> tuple[list[dict[str, Any]], dict[str, Any] | None]:
        context_output = None
        lessons_to_add = []

        if request.lessonIds:
            # Look up each lesson ID
            for l_id in request.lessonIds:
                lesson = self._find_lesson(l_id, request.courseId, state.get("lastLearningPathContext"))
                if lesson:
                    lessons_to_add.append(lesson)
        elif request.lessonId:
            lesson = self._find_lesson(request.lessonId, request.courseId, state.get("lastLearningPathContext"))
            if lesson:
                lessons_to_add.append(lesson)
        elif request.query:
            query = str(request.query).strip().lower()
            all_lessons = []

            if request.courseId:
                all_lessons = self._lessons_for_course(request.courseId, None)
            else:
                context_output = LearningPathContextService(self.db).retrieve_context(str(request.query).strip(), top_k=5)
                all_lessons = [
                    l for l in context_output.get("relevantLessons") or []
                    if isinstance(l, dict) and l.get("lessonId")
                ]

            # Smart fuzzy and type matching
            if query in ("quiz", "video", "assignment", "article"):
                lessons_to_add = [
                    l for l in all_lessons
                    if query in str(l.get("lessonTitle") or "").lower() or query == str(l.get("lessonType") or "").lower()
                ]
            else:
                # 1. Try exact match first
                lesson = next((l for l in all_lessons if str(l.get("lessonTitle") or "").lower() == query), None)
                # 2. Try partial match: query is in lesson title
                if not lesson:
                    lesson = next((l for l in all_lessons if query in str(l.get("lessonTitle") or "").lower()), None)
                # 3. Try partial match: lesson title is in query
                if not lesson:
                    lesson = next((l for l in all_lessons if str(l.get("lessonTitle") or "").lower() in query), None)
                # 4. Fallback to first lesson (only if no courseId is specified)
                if not lesson and not request.courseId:
                    lesson = all_lessons[0] if all_lessons else None
                if lesson:
                    lessons_to_add.append(lesson)
        else:
            raise ValueError("query, lessonId, or lessonIds is required for ADD_LESSON.")

        if not lessons_to_add:
            raise ValueError("No matching lessons found to add.")

        additions = self._new_items_from_lessons(lessons_to_add, items)
        if not additions:
            raise ValueError("Specified lessons are already in the proposal.")
        return items + additions, context_output

    def _lessons_for_course(self, course_id: str, context_output: dict[str, Any] | None) -> list[dict[str, Any]]:
        context_lessons = [
            lesson
            for lesson in (context_output or {}).get("relevantLessons", [])
            if isinstance(lesson, dict) and lesson.get("courseId") == course_id and lesson.get("lessonId")
        ]
        if context_lessons:
            return context_lessons
        courses = self.course_repo.get_by_ids([course_id])
        if not courses:
            raise ValueError("Course not found.")
        return _course_lessons(courses[0])

    def _find_course_by_lesson_id(self, lesson_id: str) -> Course | None:
        courses = self.course_repo.get_published_courses()
        for course in courses:
            for section in course.curriculum or []:
                for lesson in section.get("lessons", []) or []:
                    if lesson.get("id") == lesson_id:
                        return course
        return None

    def _find_lesson(
        self,
        lesson_id: str,
        course_id: str | None,
        context_output: dict[str, Any] | None,
    ) -> dict[str, Any] | None:
        for lesson in (context_output or {}).get("relevantLessons", []):
            if lesson.get("lessonId") == lesson_id and (not course_id or lesson.get("courseId") == course_id):
                return lesson

        resolved_course_id = course_id
        if not resolved_course_id:
            resolved_course = self._find_course_by_lesson_id(lesson_id)
            if resolved_course:
                resolved_course_id = resolved_course.id

        if not resolved_course_id:
            return None

        for lesson in self._lessons_for_course(resolved_course_id, context_output):
            if lesson.get("lessonId") == lesson_id:
                return lesson
        return None

    def _new_items_from_lessons(self, lessons: list[dict[str, Any]], existing_items: list[dict[str, Any]]) -> list[dict[str, Any]]:
        existing_lesson_ids = {item.get("lessonId") for item in existing_items}
        additions = []
        for lesson in lessons:
            lesson_id = lesson.get("lessonId")
            course_id = lesson.get("courseId")
            if not lesson_id or not course_id or lesson_id in existing_lesson_ids:
                continue
            existing_lesson_ids.add(lesson_id)
            additions.append(
                {
                    "courseId": course_id,
                    "courseTitle": lesson.get("courseTitle"),
                    "courseUrl": f"https://shiny.id.vn/courses/{course_id}",
                    "lessonId": lesson_id,
                    "lessonTitle": lesson.get("lessonTitle"),
                    "orderIndex": len(existing_items) + len(additions),
                    "isSuggested": True,
                }
            )
        return additions

    def create_learning_path(
        self,
        session_id: str,
        token: str | None,
        proposal_id: str | None = None,
        version: int | None = None,
    ) -> dict[str, Any]:
        state = self._state(session_id)
        proposal = dict(state.get("activeProposal") or {})
        if not proposal:
            raise ValueError("No active learning path proposal.")
        if proposal_id and proposal_id != proposal.get("proposalId"):
            raise ValueError("proposalId does not match the active proposal.")
        if version is not None and int(version) != int(proposal.get("version") or 0):
            raise ValueError("Learning path proposal has changed. Refresh before creating.")

        payload = {
            "title": proposal["title"],
            "description": proposal.get("description"),
            "items": [
                {
                    "courseId": item["courseId"],
                    "lessonId": item["lessonId"],
                    "orderIndex": index,
                    "isSuggested": bool(item.get("isSuggested", True)),
                }
                for index, item in enumerate(sorted(proposal.get("items") or [], key=lambda item: item["orderIndex"]))
            ],
        }
        response = self.commit_client.create_path(payload, token)
        if response.get("success"):
            proposal["status"] = "CREATED"
            proposal["createdLearningPathId"] = ((response.get("data") or {}).get("id"))
            state["activeProposal"] = proposal
            state["awaitingConfirmation"] = False
            state["lastConfirmationResolved"] = False
            self._save_state(session_id, state)
        return response

    def _validated_items(self, raw_items: list[dict[str, Any]], allowed_lessons: dict[str, dict[str, Any]]) -> list[dict[str, Any]]:
        items = []
        seen = set()
        for raw in raw_items:
            lesson_id = str(raw.get("lessonId") or "").strip()
            if not lesson_id or lesson_id in seen or lesson_id not in allowed_lessons:
                continue
            seen.add(lesson_id)
            lesson = allowed_lessons[lesson_id]
            items.append(
                {
                    "courseId": lesson["courseId"],
                    "courseTitle": lesson.get("courseTitle"),
                    "courseUrl": f"https://shiny.id.vn/courses/{lesson['courseId']}",
                    "lessonId": lesson_id,
                    "lessonTitle": lesson.get("lessonTitle"),
                    "orderIndex": len(items),
                    "isSuggested": bool(raw.get("isSuggested", True)),
                }
            )
        return items

    def _move_item(self, items: list[dict[str, Any]], lesson_id: str | None, order_index: int | None) -> list[dict[str, Any]]:
        if not lesson_id or order_index is None:
            raise ValueError("lessonId and orderIndex are required for MOVE_LESSON.")
        target = next((item for item in items if item.get("lessonId") == lesson_id), None)
        if target is None:
            raise ValueError("Proposal item not found.")
        remaining = [item for item in items if item.get("lessonId") != lesson_id]
        remaining.insert(max(0, min(int(order_index), len(remaining))), target)
        return remaining

    def _normalize_order(self, items: list[dict[str, Any]]) -> list[dict[str, Any]]:
        normalized = []
        seen = set()
        for item in sorted(items, key=lambda item: int(item.get("orderIndex") or 0)):
            lesson_id = item.get("lessonId")
            if not lesson_id or lesson_id in seen:
                continue
            seen.add(lesson_id)
            normalized.append({**item, "orderIndex": len(normalized)})
        return normalized

    def _to_response(self, proposal: dict[str, Any]) -> LearningPathProposalResponse:
        return LearningPathProposalResponse(
            proposalId=proposal["proposalId"],
            sessionId=proposal.get("sessionId"),
            version=int(proposal.get("version") or 1),
            title=proposal["title"],
            description=proposal.get("description"),
            courseIds=list(proposal.get("courseIds") or []),
            candidateCourseIds=list(proposal.get("candidateCourseIds") or []),
            status=proposal.get("status") or "PROPOSED",
            items=[LearningPathProposalItem(**item) for item in proposal.get("items") or []],
        )

    def _active_proposal(self, session_id: str) -> dict[str, Any]:
        proposal = self._state(session_id).get("activeProposal")
        if not isinstance(proposal, dict) or not proposal:
            raise ValueError("No active learning path proposal.")
        return proposal

    def _state(self, session_id: str) -> dict[str, Any]:
        session = self.db.get(AgentSession, session_id)
        if session is None:
            raise ValueError("Agent session not found.")
        return dict(session.state or {})

    def _save_state(self, session_id: str, state: dict[str, Any]) -> None:
        session = self.db.get(AgentSession, session_id)
        if session is None:
            raise ValueError("Agent session not found.")
        session.state = state
        session.updated_at = datetime.utcnow()
        self.db.commit()


def _course_lessons(course: Course) -> list[dict[str, Any]]:
    lessons = []
    for section in course.curriculum or []:
        section_title = section.get("title")
        for lesson in section.get("lessons", []) or []:
            lesson_id = lesson.get("id")
            if not lesson_id:
                continue
            lessons.append(
                {
                    "courseId": course.id,
                    "courseTitle": course.title,
                    "sectionTitle": section_title,
                    "lessonId": lesson_id,
                    "lessonTitle": lesson.get("title"),
                    "lessonType": lesson.get("lessonType"),
                    "duration": lesson.get("duration"),
                    "orderIndex": len(lessons),
                }
            )
    return lessons


def _order_courses(courses: list[Course], course_ids: list[str]) -> list[Course]:
    by_id = {course.id: course for course in courses}
    return [by_id[course_id] for course_id in course_ids if course_id in by_id]


def _lesson_map(lessons: list[dict[str, Any]]) -> dict[str, dict[str, Any]]:
    return {lesson["lessonId"]: lesson for lesson in lessons if lesson.get("lessonId")}


def _short_text(value: str | None, limit: int) -> str | None:
    text = " ".join(str(value or "").split())
    if not text:
        return None
    return text[:limit]


def _dedupe(values: list[str]) -> list[str]:
    result = []
    seen = set()
    for value in values:
        if value and value not in seen:
            seen.add(value)
            result.append(value)
    return result
