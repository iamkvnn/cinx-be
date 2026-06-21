from typing import Any, Literal

from pydantic import BaseModel, Field


AgentIntent = Literal[
    "GREETING",
    "COURSE_SEARCH",
    "POLICY_QA",
    "LEARNING_PATH_PROPOSAL",
    "LEARNING_PATH_PLAN",
    "LEARNING_PATH_EDIT",
    "LEARNING_PATH_COMMIT",
    "GENERAL_QA",
    "OUT_OF_SCOPE",
    "CLARIFICATION",
    "FAILED",
]


class Citation(BaseModel):
    sourceType: str
    title: str | None = None
    courseId: str | None = None
    lessonIds: list[str] = Field(default_factory=list)
    sectionTitle: str | None = None
    documentId: str | None = None
    sourceUrl: str | None = None
    score: float | None = None


class AgentChatRequest(BaseModel):
    sessionId: str | None = None
    message: str
    mode: str | None = None


class SuggestedAction(BaseModel):
    type: str
    label: str
    payload: dict[str, Any] = Field(default_factory=dict)


class AgentError(BaseModel):
    code: str
    message: str
    retryable: bool = True


class ToolCallInfo(BaseModel):
    name: str
    input: dict[str, Any] = Field(default_factory=dict)
    output: dict[str, Any] = Field(default_factory=dict)
    error: str | None = None


class LearningPathProposalItem(BaseModel):
    courseId: str
    courseTitle: str | None = None
    lessonId: str
    lessonTitle: str | None = None
    orderIndex: int
    isSuggested: bool = True


class LearningPathProposalResponse(BaseModel):
    proposalId: str
    sessionId: str | None = None
    version: int = 1
    title: str
    description: str | None = None
    courseIds: list[str]
    candidateCourseIds: list[str] = Field(default_factory=list)
    status: str = "PROPOSED"
    items: list[LearningPathProposalItem]


class AgentChatResponse(BaseModel):
    runId: str | None = None
    sessionId: str
    intent: AgentIntent
    answer: str
    error: AgentError | None = None
    citations: list[Citation] = Field(default_factory=list)
    proposal: LearningPathProposalResponse | None = None
    suggestedActions: list[SuggestedAction] = Field(default_factory=list)
    toolCalls: list[ToolCallInfo] = Field(default_factory=list)
    trace: dict[str, Any] | None = None


class CreateLearningPathProposalRequest(BaseModel):
    goal: str
    constraints: dict[str, Any] = Field(default_factory=dict)
    topK: int = 5
    sessionId: str | None = None


class ProposalUpdateRequest(BaseModel):
    version: int | None = None
    operation: Literal["ADD_COURSE", "REMOVE_COURSE", "ADD_LESSON", "REMOVE_LESSON", "MOVE_LESSON", "UPDATE_METADATA"]
    courseId: str | None = None
    courseIds: list[str] | None = None
    lessonId: str | None = None
    lessonIds: list[str] | None = None
    orderIndex: int | None = None
    query: str | None = None
    title: str | None = None
    description: str | None = None
    constraints: dict[str, Any] = Field(default_factory=dict)


class CreateLearningPathFromProposalRequest(BaseModel):
    proposalId: str | None = None
    version: int | None = None
    confirmed: bool = True


class KnowledgeImportRequest(BaseModel):
    title: str
    content: str
    sourceType: str = "CMS"
    sourceUrl: str | None = None
