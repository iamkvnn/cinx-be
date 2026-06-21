from dataclasses import dataclass
from typing import Any, Callable

from sqlalchemy.orm import Session

from app.services.llm_client import DigitalOceanLLMClient
from app.agent.services.course_search import CourseSearchService, filters_to_dict
from app.agent.services.knowledge import KnowledgeService
from app.agent.services.learning_path import LearningPathContextService, LearningPathProposalService
from app.entities.course import Course


@dataclass(frozen=True)
class ToolSpec:
    name: str
    description: str
    requires_auth: bool
    is_mutating: bool
    parameters_schema: dict[str, Any]
    execute: Callable[[dict[str, Any]], dict[str, Any]]


class ToolRegistry:
    def __init__(
        self,
        db: Session,
        token: str | None = None,
        user_id: str | None = None,
        session_id: str | None = None,
        llm_client: DigitalOceanLLMClient | None = None,
    ):
        self.db = db
        self.token = token
        self.user_id = user_id
        self.session_id = session_id
        self.llm = llm_client
        self._tools = {
            "course_search": ToolSpec(
                name="course_search",
                description="Search published courses using semantic retrieval and structured filters.",
                requires_auth=False,
                is_mutating=False,
                parameters_schema=_object_schema(
                    {
                        "query": {"type": "string"},
                        "topK": {"type": "integer", "minimum": 1, "maximum": 5},
                    },
                    ["query", "topK"],
                ),
                execute=self._course_search,
            ),
            "policy_retrieve": ToolSpec(
                name="policy_retrieve",
                description="Retrieve policy/CMS knowledge chunks.",
                requires_auth=False,
                is_mutating=False,
                parameters_schema=_object_schema({"query": {"type": "string"}}, ["query"]),
                execute=self._policy_retrieve,
            ),
            "learning_path_retrieve_context": ToolSpec(
                name="learning_path_retrieve_context",
                description="Retrieve relevant courses, sections, and lessons for a learning path proposal. Does not create or save a path.",
                requires_auth=False,
                is_mutating=False,
                parameters_schema=_object_schema(
                    {
                        "goal": {"type": "string"},
                        "topK": {"type": "integer", "minimum": 1, "maximum": 5},
                        "contextCourseIds": {
                            "type": "array",
                            "items": {"type": "string"},
                            "maxItems": 5,
                        },
                    },
                    ["goal", "topK", "contextCourseIds"],
                ),
                execute=self._learning_path_retrieve_context,
            ),
            "course_get_details": ToolSpec(
                name="course_get_details",
                description="Retrieve the full details and curriculum (sections and lessons) of a specific course by its courseId.",
                requires_auth=False,
                is_mutating=False,
                parameters_schema=_object_schema(
                    {"courseId": {"type": "string"}},
                    ["courseId"],
                ),
                execute=self._course_get_details,
            ),
            "learning_path_update_proposal": ToolSpec(
                name="learning_path_update_proposal",
                description="Update the active in-session learning path proposal.",
                requires_auth=False,
                is_mutating=True,
                parameters_schema=_object_schema(
                    {
                        "proposalId": {"type": ["string", "null"]},
                        "version": {"type": ["integer", "null"]},
                        "operation": {
                            "type": "string",
                            "enum": [
                                "ADD_COURSE",
                                "REMOVE_COURSE",
                                "ADD_LESSON",
                                "REMOVE_LESSON",
                                "MOVE_LESSON",
                                "UPDATE_METADATA",
                            ],
                        },
                        "courseId": {"type": ["string", "null"]},
                        "courseIds": {
                            "type": ["array", "null"],
                            "items": {"type": "string"},
                        },
                        "lessonId": {"type": ["string", "null"]},
                        "lessonIds": {
                            "type": ["array", "null"],
                            "items": {"type": "string"},
                        },
                        "orderIndex": {"type": ["integer", "null"]},
                        "query": {"type": ["string", "null"]},
                        "title": {"type": ["string", "null"]},
                        "description": {"type": ["string", "null"]},
                        "constraints": _object_schema({}, []),
                    },
                    ["proposalId", "version", "operation"],
                ),
                execute=self._learning_path_update_proposal,
            ),
            "learning_path_create": ToolSpec(
                name="learning_path_create",
                description="Create the confirmed active learning path proposal in the learning service.",
                requires_auth=True,
                is_mutating=True,
                parameters_schema=_object_schema(
                    {
                        "proposalId": {"type": ["string", "null"]},
                        "version": {"type": ["integer", "null"]},
                        "confirmed": {"type": "boolean"},
                    },
                    ["proposalId", "version", "confirmed"],
                ),
                execute=self._learning_path_create,
            ),
        }

    def get(self, name: str) -> ToolSpec | None:
        return self._tools.get(name)

    def openai_tools(self, names: list[str] | set[str] | None = None) -> list[dict[str, Any]]:
        allowed = set(names) if names is not None else set(self._tools.keys())
        return [
            {
                "type": "function",
                "name": tool.name,
                "description": tool.description,
                "parameters": tool.parameters_schema,
                "strict": True,
            }
            for tool in self._tools.values()
            if tool.name in allowed
        ]

    def execute(self, name: str, input_data: dict[str, Any]) -> dict[str, Any]:
        tool = self.get(name)
        if tool is None:
            raise ValueError(f"Unknown tool: {name}")
        self._validate_input(tool, input_data)
        if tool.requires_auth and not self.token:
            raise ValueError("Bearer token is required for this tool.")
        return tool.execute(input_data)

    def _validate_input(self, tool: ToolSpec, input_data: dict[str, Any]) -> None:
        if not isinstance(input_data, dict):
            raise ValueError(f"Tool input for {tool.name} must be an object.")
        allowed = set(tool.parameters_schema.get("properties", {}).keys())
        extra = set(input_data.keys()) - allowed
        if extra:
            raise ValueError(f"Tool input for {tool.name} contains unsupported fields: {sorted(extra)}")
        required = tool.parameters_schema.get("required", [])
        missing = [field for field in required if field not in input_data]
        if missing:
            raise ValueError(f"Tool input for {tool.name} is missing required fields: {missing}")
        if tool.name in {"course_search", "policy_retrieve"} and not str(input_data.get("query") or "").strip():
            raise ValueError("query is required.")
        if tool.name == "learning_path_retrieve_context" and not str(input_data.get("goal") or "").strip():
            raise ValueError("goal is required.")
        if tool.name in {"course_search", "learning_path_retrieve_context"}:
            int(input_data.get("topK"))
        if tool.name in {"learning_path_update_proposal", "learning_path_create"} and not self.session_id:
            raise ValueError("sessionId is required for proposal tools.")
        if tool.name == "learning_path_update_proposal":
            op = input_data.get("operation")
            if not op:
                raise ValueError("operation is required.")
            if not isinstance(op, (str, dict)):
                raise ValueError("operation must be a string or object.")
        if tool.name == "learning_path_create" and not isinstance(input_data.get("confirmed"), bool):
            raise ValueError("confirmed must be a boolean.")

    def _course_search(self, input_data: dict[str, Any]) -> dict[str, Any]:
        query = str(input_data.get("query") or "")
        top_k = _clamp_top_k(input_data.get("topK"), default=5)
        results, citations, filters = CourseSearchService(self.db, llm_client=self.llm).search(query, top_k=top_k)
        return {
            "results": results,
            "citations": [citation.model_dump() for citation in citations],
            "filters": filters_to_dict(filters),
        }

    def _policy_retrieve(self, input_data: dict[str, Any]) -> dict[str, Any]:
        query = str(input_data.get("query") or "")
        contexts, citations = KnowledgeService(self.db).retrieve(query)
        return {
            "contexts": contexts,
            "citations": [citation.model_dump() for citation in citations],
        }

    def _learning_path_retrieve_context(self, input_data: dict[str, Any]) -> dict[str, Any]:
        goal = str(input_data.get("goal") or input_data.get("query") or "")
        top_k = _clamp_top_k(input_data.get("topK"), default=5)
        context_course_ids = [str(course_id) for course_id in input_data.get("contextCourseIds") or [] if course_id]
        return LearningPathContextService(self.db).retrieve_context(
            goal,
            top_k=top_k,
            context_course_ids=context_course_ids,
        )

    def _learning_path_update_proposal(self, input_data: dict[str, Any]) -> dict[str, Any]:
        from app.agent.schemas import ProposalUpdateRequest

        op = input_data.get("operation")
        if isinstance(op, dict):
            raw_operation = op
        else:
            raw_operation = {
                k: v for k, v in input_data.items()
                if k not in {"proposalId", "version"}
            }

        request = ProposalUpdateRequest(
            version=input_data.get("version"),
            **raw_operation,
        )
        proposal = LearningPathProposalService(self.db, llm_client=self.llm).update_proposal(str(self.session_id), request)
        return {"proposal": proposal.model_dump()}

    def _learning_path_create(self, input_data: dict[str, Any]) -> dict[str, Any]:
        confirmed = bool(input_data.get("confirmed"))
        if not confirmed:
            raise ValueError("Explicit confirmation is required before creating a learning path.")
        return LearningPathProposalService(self.db, llm_client=self.llm).create_learning_path(
            str(self.session_id),
            self.token,
            proposal_id=input_data.get("proposalId"),
            version=input_data.get("version"),
        )

    def _course_get_details(self, input_data: dict[str, Any]) -> dict[str, Any]:
        course_id = str(input_data.get("courseId") or "").strip()
        if not course_id:
            raise ValueError("courseId is required.")
        course = self.db.get(Course, course_id)
        if not course or course.status != "PUBLISHED":
            raise ValueError("Course not found or is not published.")
        
        lessons = []
        for section in course.curriculum or []:
            section_title = section.get("title")
            for lesson in section.get("lessons", []) or []:
                lesson_id = lesson.get("id")
                if not lesson_id:
                    continue
                lessons.append({
                    "courseId": course.id,
                    "courseTitle": course.title,
                    "sectionTitle": section_title,
                    "lessonId": lesson_id,
                    "lessonTitle": lesson.get("title"),
                    "lessonType": lesson.get("lessonType"),
                    "duration": lesson.get("duration"),
                })
                
        return {
            "courseId": course.id,
            "title": course.title,
            "description": course.description,
            "categoryName": course.category_name,
            "instructorName": course.instructor_name,
            "rating": course.rating,
            "price": course.price,
            "discountedPrice": course.discounted_price,
            "duration": course.duration,
            "hasCertificate": course.has_certificate,
            "lessons": lessons,
        }

def _object_schema(properties: dict[str, Any], required: list[str]) -> dict[str, Any]:
    return {
        "type": "object",
        "properties": properties,
        "required": required,
        "additionalProperties": False,
    }


def _clamp_top_k(value, default: int = 5) -> int:
    try:
        top_k = int(value or default)
    except (TypeError, ValueError):
        top_k = default
    return max(1, min(top_k, 5))
