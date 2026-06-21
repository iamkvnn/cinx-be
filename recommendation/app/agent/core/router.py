import re
from dataclasses import dataclass
from typing import Any

from app.agent.schemas import AgentIntent
from app.services.llm_client import LLMRetryError


@dataclass(frozen=True)
class AgentRouteDecision:
    intent: AgentIntent
    confidence: float = 0.0
    nextFlow: str = "direct_answer_flow"
    allowedTools: tuple[str, ...] = ()
    requiresConfirmation: bool = False
    question: str | None = None
    reason: str | None = None


FLOW_TOOL_ALLOWLIST: dict[str, tuple[str, ...]] = {
    "course_search_flow": ("course_search", "course_get_details"),
    "policy_qa_flow": ("policy_retrieve",),
    "learning_path_proposal_flow": ("course_search", "course_get_details"),
    "learning_path_edit_flow": ("learning_path_update_proposal", "course_get_details", "course_search"),
    "learning_path_commit_flow": ("learning_path_create",),
    "direct_answer_flow": (),
    "clarification_flow": (),
    "scoped_refusal_flow": (),
}

INTENT_FLOW: dict[str, str] = {
    "GREETING": "direct_answer_flow",
    "GENERAL_QA": "direct_answer_flow",
    "OUT_OF_SCOPE": "scoped_refusal_flow",
    "CLARIFICATION": "clarification_flow",
    "COURSE_SEARCH": "course_search_flow",
    "POLICY_QA": "policy_qa_flow",
    "LEARNING_PATH_PROPOSAL": "learning_path_proposal_flow",
    "LEARNING_PATH_PLAN": "learning_path_proposal_flow",
    "LEARNING_PATH_EDIT": "learning_path_edit_flow",
    "LEARNING_PATH_COMMIT": "learning_path_commit_flow",
}

DIRECT_INTENTS = {"GREETING", "GENERAL_QA", "OUT_OF_SCOPE", "CLARIFICATION"}
ALLOWED_ROUTER_INTENTS = set(INTENT_FLOW.keys())


def route_decision_from_llm(parsed: dict[str, Any], max_attempts: int) -> AgentRouteDecision:
    intent = normalize_intent(parsed.get("intent"))
    confidence = float_or_zero(parsed.get("confidence"))
    reason = parsed.get("reason")

    if intent not in ALLOWED_ROUTER_INTENTS:
        raise LLMRetryError(f"Invalid intent from LLM: {intent}", max_attempts)

    if confidence < 0.55:
        return AgentRouteDecision(
            "CLARIFICATION",
            confidence=confidence,
            nextFlow="clarification_flow",
            question=parsed.get("question"),
            reason=reason,
        )

    return AgentRouteDecision(
        intent,
        confidence=confidence,
        nextFlow=str(parsed.get("nextFlow") or INTENT_FLOW.get(intent, "clarification_flow")),
        allowedTools=tuple(str(tool) for tool in (parsed.get("allowedTools") or [])),
        requiresConfirmation=bool(parsed.get("requiresConfirmation")),
        question=parsed.get("question"),
        reason=reason,
    )


def validate_route_decision(decision: AgentRouteDecision) -> AgentRouteDecision:
    expected_flow = INTENT_FLOW.get(decision.intent)
    if expected_flow is None:
        return clarification_decision(
            decision,
            None,
            f"Unsupported intent: {decision.intent}",
        )

    next_flow = decision.nextFlow if decision.nextFlow in FLOW_TOOL_ALLOWLIST else expected_flow
    if next_flow != expected_flow:
        return clarification_decision(
            decision,
            None,
            f"Router flow {decision.nextFlow} did not match intent {decision.intent}.",
        )

    allowed_by_flow = FLOW_TOOL_ALLOWLIST[next_flow]
    if any(tool not in allowed_by_flow for tool in decision.allowedTools):
        return clarification_decision(
            decision,
            None,
            "Router returned unsupported tools.",
        )

    return AgentRouteDecision(
        intent="LEARNING_PATH_PROPOSAL" if decision.intent == "LEARNING_PATH_PLAN" else decision.intent,
        confidence=decision.confidence,
        nextFlow=next_flow,
        allowedTools=decision.allowedTools or allowed_by_flow,
        requiresConfirmation=decision.requiresConfirmation or decision.intent == "LEARNING_PATH_COMMIT",
        question=decision.question,
        reason=decision.reason,
    )


def apply_actionability_gate(
    decision: AgentRouteDecision,
    gate_result: dict[str, Any],
) -> AgentRouteDecision:
    """Apply the result of the LLM actionability gate to a route decision.

    If the gate says the request is not actionable (missing fields), return a
    clarification decision carrying the LLM-generated question.  Otherwise
    return the original decision unchanged.
    """
    if decision.question or not decision.allowedTools:
        return decision

    actionable = bool(gate_result.get("actionable", True))
    if actionable:
        return decision

    question = gate_result.get("question") or None
    reason = gate_result.get("reason") or "Missing required information."
    return clarification_decision(decision, question, reason)


def prepare_commit_route(
    decision: AgentRouteDecision,
    message: str,
    state: dict[str, Any],
    confirmation: bool,
) -> AgentRouteDecision:
    proposal_id = extract_proposal_id(message) or (state.get("activeProposal") or {}).get("proposalId")
    if not proposal_id:
        return AgentRouteDecision(
            "CLARIFICATION",
            confidence=decision.confidence,
            nextFlow="clarification_flow",
            question=None,
            reason="Missing active proposal for learning path creation.",
        )
    if not confirmation:
        return AgentRouteDecision(
            "LEARNING_PATH_COMMIT",
            confidence=decision.confidence,
            nextFlow="learning_path_commit_flow",
            requiresConfirmation=True,
            question=None,
            reason="Commit requires explicit confirmation.",
        )
    proposal = dict(state.get("activeProposal") or {})
    proposal["proposalId"] = proposal_id
    state["activeProposal"] = proposal
    return decision


def resolved_confirmation(message: str, state: dict[str, Any]) -> bool:
    """Delegate confirmation detection entirely to the LLM actionability gate.

    Code only checks the awaitingConfirmation flag; the gate will decide
    whether the latest message constitutes explicit confirmation.
    """
    return bool(state.get("awaitingConfirmation") and state.get("lastConfirmationResolved"))


def validate_user_message(message: str) -> str:
    cleaned = str(message or "").strip()
    if not cleaned:
        raise ValueError("message is required.")
    if len(cleaned) > 8000:
        raise ValueError("message is too long.")
    return cleaned


def clarification_decision(base: AgentRouteDecision, question: str | None, reason: str) -> AgentRouteDecision:
    return AgentRouteDecision(
        "CLARIFICATION",
        confidence=base.confidence,
        nextFlow="clarification_flow",
        question=question,
        reason=reason,
    )


def normalize_intent(intent: Any) -> str:
    if intent == "LEARNING_PATH_PLAN":
        return "LEARNING_PATH_PROPOSAL"
    return str(intent or "")


def float_or_zero(value: Any) -> float:
    try:
        return float(value)
    except (TypeError, ValueError):
        return 0.0


def extract_proposal_id(message: str) -> str | None:
    match = re.search(r"\bproposal[-_][A-Za-z0-9-]+\b", message)
    return match.group(0) if match else None
