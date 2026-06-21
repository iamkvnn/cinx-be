import json
import re
from typing import Any

from app.agent.schemas import (
    AgentChatResponse,
    SuggestedAction,
    ToolCallInfo,
    Citation,
    AgentIntent,
)
from app.agent.core.prompts import direct_answer_prompt
from app.agent.core.router import AgentRouteDecision
from app.services.llm_client import DigitalOceanLLMClient


class GroundingValidator:
    def validate(self, answer: str, tool_output: dict) -> tuple[bool, str | None]:
        if contains_raw_tool_syntax(answer):
            return False, "Answer exposed raw tool call syntax."
        allowed_ids = self._allowed_ids(tool_output)
        mentioned_ids = set(re.findall(r"\b(?:course|les|lesson|path|proposal)[-_][A-Za-z0-9_-]+\b", answer))
        invented = [value for value in mentioned_ids if value not in allowed_ids]
        if invented:
            return False, f"Answer mentioned IDs not present in tool output: {invented}"
        return True, None

    def _allowed_ids(self, value) -> set[str]:
        found = set()
        if isinstance(value, dict):
            for key, item in value.items():
                if key.lower().endswith("id") and isinstance(item, str):
                    found.add(item)
                found.update(self._allowed_ids(item))
        elif isinstance(value, list):
            for item in value:
                found.update(self._allowed_ids(item))
        elif isinstance(value, str):
            try:
                parsed = json.loads(value)
            except json.JSONDecodeError:
                return found
            found.update(self._allowed_ids(parsed))
        return found


def contains_raw_tool_syntax(answer: str) -> bool:
    text = str(answer or "").lower()
    markers = (
        "<｜dsml｜",
        "<|dsml|",
        "tool_calls",
        "invoke name=",
        "function_call",
        "function_call_output",
    )
    return any(marker in text for marker in markers)


def direct_route_response(
    llm: DigitalOceanLLMClient,
    session_id: str,
    run_id: str,
    message: str,
    decision: AgentRouteDecision,
) -> AgentChatResponse:
    if decision.intent == "CLARIFICATION" or decision.question:
        answer = decision.question or ""
        # If the question is still empty (e.g. came from validate_route_decision with no LLM question),
        # ask the LLM to produce a clarifying question in the user's language.
        if not answer.strip():
            answer = llm.generate_text_required(
                direct_answer_prompt(message),
                system=(
                    "You are CINX learning assistant. The user's request is ambiguous or lacks required information. "
                    "Ask a clarifying question in the same language as the user message. Do not answer with tool data."
                ),
                temperature=0.2,
                label="direct_clarification_answer",
            )
        return AgentChatResponse(
            runId=run_id,
            sessionId=session_id,
            intent=decision.intent,
            answer=answer,
            suggestedActions=[],
            toolCalls=[],
        )

    # GREETING, OUT_OF_SCOPE, GENERAL_QA — all handled by LLM in user's language
    system_hint = {
        "GREETING": "You are CINX learning assistant. Respond to the greeting warmly and briefly in the same language as the user message. Mention you can help find courses, answer policy questions, or create a learning path.",
        "OUT_OF_SCOPE": "You are CINX learning assistant. Politely decline and explain you can only help with CINX courses, learning paths, enrollment/payment policy, certificates, and related learning questions. Respond in the same language as the user message.",
    }.get(
        decision.intent,
        "You are CINX learning assistant. Answer without using tools. Respond in the same language as the user message.",
    )

    answer = llm.generate_text_required(
        direct_answer_prompt(message),
        system=system_hint,
        temperature=0.2,
        label="direct_answer",
    )
    return AgentChatResponse(
        runId=run_id,
        sessionId=session_id,
        intent=decision.intent,
        answer=answer,
        suggestedActions=[],
        toolCalls=[],
    )


def generate_suggested_actions(
    llm,
    message: str,
    intent: AgentIntent,
    answer: str,
    tool_calls: list[ToolCallInfo],
    active_proposal: dict[str, Any] | None = None,
) -> list[SuggestedAction]:
    return []

