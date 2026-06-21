import uuid
from datetime import datetime
from typing import Any

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.agent.core.router import AgentRouteDecision
from app.agent.schemas import ToolCallInfo
from app.entities.agent_session import AgentMessage, AgentRun, AgentSession


DEFAULT_SESSION_STATE = {
    "currentFlow": None,
    "lastIntent": None,
    "lastCourseIds": [],
    "lastSearchQuery": None,
    "lastCourseSummaries": [],
    "activeProposal": None,
    "pendingQuestion": None,
    "awaitingConfirmation": False,
    "lastConfirmationResolved": False,
}


class AgentSessionStateStore:
    def __init__(self, db: Session):
        self.db = db

    def load(self, session_id: str) -> dict[str, Any]:
        session = self.db.get(AgentSession, session_id)
        raw_state = getattr(session, "state", None) if session else None
        state = DEFAULT_SESSION_STATE.copy()
        if isinstance(raw_state, dict):
            state.update(raw_state)
        state["lastCourseIds"] = list(state.get("lastCourseIds") or [])
        state["lastCourseSummaries"] = list(state.get("lastCourseSummaries") or [])
        state.pop("pendingActions", None)
        if not isinstance(state.get("activeProposal"), dict):
            state["activeProposal"] = None
        return state

    def save(self, session_id: str, state: dict[str, Any]) -> None:
        session = self.db.get(AgentSession, session_id)
        if not session:
            return
        session.state = state
        session.updated_at = datetime.utcnow()
        self.db.commit()


def ensure_session(db: Session, session_id: str | None, user_id: str | None) -> str:
    if session_id:
        session = db.get(AgentSession, session_id)
        if session:
            session.updated_at = datetime.utcnow()
            db.commit()
            return session_id

    new_id = session_id or str(uuid.uuid4())
    db.add(AgentSession(id=new_id, user_id=user_id, created_at=datetime.utcnow(), updated_at=datetime.utcnow()))
    db.commit()
    return new_id


def start_run(db: Session, session_id: str, user_id: str | None) -> AgentRun:
    run = AgentRun(id=str(uuid.uuid4()), session_id=session_id, user_id=user_id, status="STARTED")
    db.add(run)
    db.commit()
    return run


def finish_run(
    db: Session,
    run: AgentRun,
    status: str,
    trace: dict | None = None,
    error: dict | None = None,
) -> None:
    run.status = status
    run.trace = trace
    run.error = error
    run.updated_at = datetime.utcnow()
    db.commit()


def save_message(
    db: Session,
    session_id: str,
    role: str,
    content: str,
    intent: str | None = None,
    tool_calls: list[ToolCallInfo] | None = None,
    citations=None,
) -> None:
    db.add(
        AgentMessage(
            id=str(uuid.uuid4()),
            session_id=session_id,
            role=role,
            content=content,
            intent=intent,
            tool_calls=[tool_call.model_dump(exclude_none=True) for tool_call in tool_calls] if tool_calls else None,
            citations=[citation.model_dump(exclude_none=True) for citation in citations] if citations else None,
            created_at=datetime.utcnow(),
        )
    )
    db.commit()


def get_recent_messages(db: Session, session_id: str, limit: int = 8) -> list[dict[str, str]]:
    stmt = (
        select(AgentMessage)
        .where(AgentMessage.session_id == session_id)
        .order_by(AgentMessage.created_at.desc())
        .limit(limit)
    )
    messages = list(reversed(db.execute(stmt).scalars().all()))
    return [{"role": message.role, "content": message.content} for message in messages]


def state_after_direct_response(state: dict[str, Any], decision: AgentRouteDecision) -> dict[str, Any]:
    next_state = state.copy()
    next_state["currentFlow"] = decision.nextFlow
    next_state["lastIntent"] = decision.intent
    next_state["pendingQuestion"] = decision.question
    next_state["awaitingConfirmation"] = bool(decision.requiresConfirmation or decision.intent == "LEARNING_PATH_COMMIT")
    next_state["lastConfirmationResolved"] = False
    next_state.pop("pendingActions", None)
    return next_state


def state_after_tool_calls(
    state: dict[str, Any],
    decision: AgentRouteDecision,
    tool_calls: list[ToolCallInfo],
) -> dict[str, Any]:
    next_state = state.copy()
    next_state["currentFlow"] = decision.nextFlow
    next_state["lastIntent"] = decision.intent
    next_state["pendingQuestion"] = None
    next_state["awaitingConfirmation"] = False
    next_state["lastConfirmationResolved"] = False
    next_state.pop("pendingActions", None)
    for tool_call in tool_calls:
        if tool_call.name == "course_search":
            results = [course for course in tool_call.output.get("results", []) if isinstance(course, dict)]
            next_state["lastSearchQuery"] = str(tool_call.input.get("query") or "").strip() or next_state.get("lastSearchQuery")
            next_state["lastCourseIds"] = [course["courseId"] for course in results if course.get("courseId")][:5]
            next_state["lastCourseSummaries"] = [
                {
                    "courseId": course.get("courseId"),
                    "title": course.get("title"),
                    "categoryName": course.get("categoryName"),
                }
                for course in results
                if course.get("courseId")
            ][:5]
        if tool_call.name == "learning_path_retrieve_context":
            next_state["lastCourseIds"] = list(tool_call.output.get("candidateCourseIds") or next_state.get("lastCourseIds") or [])[:5]
            next_state["lastLearningPathContext"] = tool_call.output
            next_state["lastSearchQuery"] = str(tool_call.output.get("goal") or tool_call.input.get("goal") or next_state.get("lastSearchQuery") or "").strip() or None
        if "proposal" in tool_call.output:
            proposal = tool_call.output["proposal"]
            next_state["activeProposal"] = proposal
            next_state["lastCourseIds"] = list(proposal.get("courseIds") or next_state.get("lastCourseIds") or [])[:5]
            next_state["awaitingConfirmation"] = True
        if tool_call.name == "learning_path_create" and tool_call.output.get("success"):
            proposal = dict(next_state.get("activeProposal") or {})
            proposal["status"] = "CREATED"
            proposal["createdLearningPathId"] = ((tool_call.output.get("data") or {}).get("id"))
            next_state["activeProposal"] = proposal
            next_state["awaitingConfirmation"] = False
    return next_state


def state_mark_confirmation_resolved(state: dict[str, Any]) -> dict[str, Any]:
    """Mark that the latest user message was an explicit confirmation."""
    next_state = state.copy()
    next_state["lastConfirmationResolved"] = True
    return next_state


def public_state_context(state: dict[str, Any]) -> dict[str, Any]:
    proposal = state.get("activeProposal") or {}
    return {
        "currentFlow": state.get("currentFlow"),
        "lastIntent": state.get("lastIntent"),
        "lastCourseIds": state.get("lastCourseIds") or [],
        "lastSearchQuery": state.get("lastSearchQuery"),
        "lastCourseSummaries": state.get("lastCourseSummaries") or [],
        "activeProposalId": proposal.get("proposalId"),
        "activeProposalVersion": proposal.get("version"),
        "pendingQuestion": state.get("pendingQuestion"),
        "awaitingConfirmation": bool(state.get("awaitingConfirmation")),
    }


def context_course_ids(state: dict[str, Any]) -> list[str]:
    """Return the IDs of previously discussed courses from session state.

    Replaces goal_with_course_context() which appended an English sentence
    to the goal string.  Callers now pass context_course_ids separately so
    the LLM can decide how to incorporate it without a language assumption.
    """
    return list(state.get("lastCourseIds") or [])
