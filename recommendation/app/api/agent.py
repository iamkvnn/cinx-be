from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session

from app.agent.agent_service import AgentService
from app.agent.auth import RequestUser, current_user
from app.agent.services.knowledge import KnowledgeService
from app.agent.services.learning_path import LearningPathProposalService
from app.agent.schemas import (
    AgentChatRequest,
    CreateLearningPathFromProposalRequest,
    KnowledgeImportRequest,
    ProposalUpdateRequest,
)
from app.agent.sse import sse_event
from app.core.database import get_db
from app.core.problem import ProblemDetailException, problem_detail_body


router = APIRouter(prefix="/api/v1/recommendations/agent", tags=["agent"])
internal_router = APIRouter(prefix="/internal/recommendations", tags=["internal-recommendations"])


@router.post("/chat")
def chat_stream(
    request: AgentChatRequest,
    db: Session = Depends(get_db),
    user: RequestUser = Depends(current_user),
):
    def events():
        service = AgentService(db)
        try:
            for event in service.stream_chat(
                request.message,
                session_id=request.sessionId,
                user_id=user.user_id,
                token=user.token,
                debug=request.mode == "debug",
            ):
                yield sse_event(event["event"], event["data"])
        except ProblemDetailException as exc:
            body = problem_detail_body(
                status=exc.status,
                code=exc.code,
                detail=exc.detail,
                instance="/api/v1/recommendations/agent/chat",
                retryable=exc.retryable,
            )
            yield sse_event("error", _error_event(request.sessionId, body))
        except Exception:
            body = problem_detail_body(
                status=500,
                code="INTERNAL_ERROR",
                detail="An unexpected error occurred",
                instance="/api/v1/recommendations/agent/chat",
                retryable=False,
            )
            yield sse_event("error", _error_event(request.sessionId, body))

    return StreamingResponse(events(), media_type="text/event-stream")


@router.get("/sessions/{session_id}/proposal")
def get_learning_path_proposal(
    session_id: str,
    db: Session = Depends(get_db),
    user: RequestUser = Depends(current_user),
):
    try:
        return LearningPathProposalService(db).get_proposal(session_id)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc


@router.patch("/sessions/{session_id}/proposal")
def update_learning_path_proposal(
    session_id: str,
    request: ProposalUpdateRequest,
    db: Session = Depends(get_db),
    user: RequestUser = Depends(current_user),
):
    try:
        return LearningPathProposalService(db).update_proposal(session_id, request)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/sessions/{session_id}/proposal/create")
def create_learning_path_from_proposal(
    session_id: str,
    request: CreateLearningPathFromProposalRequest,
    db: Session = Depends(get_db),
    user: RequestUser = Depends(current_user),
):
    try:
        return LearningPathProposalService(db).create_learning_path(
            session_id,
            user.token,
            proposal_id=request.proposalId,
            version=request.version,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/knowledge/import")
def import_knowledge(
    request: KnowledgeImportRequest,
    db: Session = Depends(get_db),
    user: RequestUser = Depends(current_user),
):
    document = KnowledgeService(db).import_document(
        title=request.title,
        content=request.content,
        source_type=request.sourceType,
        source_url=request.sourceUrl,
    )
    return {"success": True, "documentId": document.id}


@internal_router.post("/knowledge/sync")
def sync_knowledge(
    request: KnowledgeImportRequest,
    db: Session = Depends(get_db),
):
    document = KnowledgeService(db).import_document(
        title=request.title,
        content=request.content,
        source_type=request.sourceType,
        source_url=request.sourceUrl,
    )
    return {"success": True, "documentId": document.id}


def _error_event(session_id: str | None, body: dict) -> dict:
    return {
        "type": "error",
        "messageId": None,
        "runId": None,
        "sessionId": session_id,
        "seq": 1,
        "timestamp": datetime.utcnow().isoformat(),
        "data": body,
    }
