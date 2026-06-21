import json
from datetime import datetime
from typing import Any, Generator, Iterator
from uuid import uuid4

from sqlalchemy.orm import Session

from app.agent.schemas import AgentChatResponse, AgentIntent, SuggestedAction, ToolCallInfo
from app.services.llm_client import DigitalOceanLLMClient, LLMRetryError
from app.core.problem import ProblemDetailException
from app.entities.agent_session import AgentRun

# Import core agent components
from app.agent.core.router import (
    AgentRouteDecision,
    DIRECT_INTENTS,
    route_decision_from_llm,
    validate_route_decision,
    apply_actionability_gate,
    prepare_commit_route,
    validate_user_message,
    extract_proposal_id,
)
from app.agent.core.session import (
    AgentSessionStateStore,
    ensure_session,
    start_run,
    finish_run,
    save_message,
    get_recent_messages,
    state_after_direct_response,
    state_after_tool_calls,
    state_mark_confirmation_resolved,
)
from app.agent.core.output import (
    direct_route_response,
    generate_suggested_actions,
    GroundingValidator,
)
from app.agent.core.tools import ToolRegistry
from app.agent.core.executor import AgentExecutor
from app.agent.core.prompts import (
    intent_prompt,
    actionability_prompt,
    confirmation_prompt,
)
from app.agent.services.learning_path import LearningPathProposalService


LLM_UNAVAILABLE_DETAIL = "The AI service is temporarily unavailable. Please try again later."
AGENT_EXECUTION_DETAIL = "The agent could not process this request."


class AgentService:
    def __init__(self, db: Session, llm_client: DigitalOceanLLMClient | None = None):
        self.db = db
        self.llm = llm_client or DigitalOceanLLMClient()
        self.grounding = GroundingValidator()
        self.state_store = AgentSessionStateStore(db)

    def stream_chat(
        self,
        message: str,
        session_id: str | None = None,
        user_id: str | None = None,
        token: str | None = None,
        debug: bool = False,
    ) -> Generator[
        dict[str, Any],
        None,
        tuple[list[ToolCallInfo], str, list[str], list[dict[str, Any]]],
    ]:
        self.llm.clear_retry_events()
        if hasattr(self.llm, "clear_llm_calls"):
            self.llm.clear_llm_calls()
        run = None
        trace: dict[str, Any] = {}

        try:
            message = self._validate_user_message(message)
            session_id = self._ensure_session(session_id, user_id)
            run = self._start_run(session_id, user_id)
            self._save_message(session_id, "user", message)
            state = self._load_session_state(session_id)
            trace["stateBefore"] = state.copy()

            stream = _StreamContext(run.id, session_id)
            yield stream.event("run_started", {"status": "started"})
            decision = self.classify_intent_decision(message, session_id=session_id)
            decision = self._validate_route_decision(decision)
            trace["routerDecision"] = decision.__dict__
            trace["allowedTools"] = list(decision.allowedTools)
            run.intent = decision.intent

            # --- Commit flow: LLM decides confirmation ---
            if decision.intent == "LEARNING_PATH_COMMIT":
                confirmation = self._resolve_confirmation_via_llm(message, state, session_id)
                decision = self._prepare_commit_route(decision, message, state, confirmation)
                if confirmation:
                    state = state_mark_confirmation_resolved(state)
                trace["routerDecision"] = decision.__dict__
                trace["allowedTools"] = list(decision.allowedTools)

            # --- LLM actionability gate ---
            if decision.allowedTools and not decision.question:
                decision = self._apply_actionability_gate(decision, message, state, session_id)
                trace["routerDecision"] = decision.__dict__
                trace["allowedTools"] = list(decision.allowedTools)

            yield stream.event(
                "intent_detected",
                {
                    "intent": decision.intent,
                    "confidence": decision.confidence,
                    "nextFlow": decision.nextFlow,
                    "allowedTools": list(decision.allowedTools),
                },
            )

            if decision.intent in DIRECT_INTENTS or decision.question or not decision.allowedTools:
                response = self._direct_route_response(session_id, run.id, message, decision)
                state = self._state_after_direct_response(state, decision)
                self._save_session_state(session_id, state)
                self._attach_llm_trace(trace)
                trace["stateAfter"] = state.copy()
                trace = self._compact_trace(trace)
                response.trace = trace if debug else None
                self._finish_run(run, "COMPLETED", trace=trace)
                self._save_message(session_id, "assistant", response.answer, response.intent, response.toolCalls, response.citations)
                if decision.requiresConfirmation:
                    yield stream.event(
                        "action_required",
                        {
                            "reason": "confirmation_required",
                            "intent": decision.intent,
                        },
                    )
                yield from self._stream_completed_response(response, stream, debug=debug)
                return

            self._finish_run(run, "PLANNING", trace=trace)
            tool_registry = ToolRegistry(self.db, token=token, user_id=user_id, session_id=session_id, llm_client=self.llm)
            
            executor = AgentExecutor(
                db=self.db,
                llm=self.llm,
                registry=tool_registry,
                session_id=session_id,
                intent=decision.intent,
                decision=decision,
                state=state,
                run=run,
                trace=trace,
                recent_messages_fn=self._recent_messages,
                grounding_validator=self.grounding,
            )
            executor_events = executor.run_stream()
            while True:
                try:
                    raw_event = next(executor_events)
                except StopIteration as completed:
                    tool_calls, final_answer, response_ids, input_items = completed.value
                    break
                yield from self._stream_executor_event(raw_event, stream)
            
            self._finish_run(run, "FINALIZING", trace=trace)
            trace["responseIds"] = response_ids
            trace["manualContextSize"] = len(input_items)
            state = self._state_after_tool_calls(state, decision, tool_calls)
            self._save_session_state(session_id, state)
            response = self._build_agent_response(
                message=message,
                session_id=session_id,
                run_id=run.id,
                intent=decision.intent,
                answer=final_answer,
                tool_calls=tool_calls,
                debug=debug,
                trace=trace,
            )
            self._attach_llm_trace(trace)
            trace["stateAfter"] = self._load_session_state(session_id).copy()
            trace = self._compact_trace(trace)
            if debug:
                response.trace = trace
            self._finish_run(run, "COMPLETED", trace=trace)
            self._save_message(session_id, "assistant", response.answer, response.intent, response.toolCalls, response.citations)
            yield from self._stream_completed_response(response, stream, debug=debug)
        except LLMRetryError as exc:
            self._attach_llm_trace(trace)
            trace = self._compact_trace(trace)
            if run is not None:
                self._finish_run(
                    run,
                    "FAILED",
                    trace=trace,
                    error={"code": "LLM_UNAVAILABLE", "message": str(exc), "retryable": True},
                )
            raise ProblemDetailException(
                status=503,
                code="LLM_UNAVAILABLE",
                detail=LLM_UNAVAILABLE_DETAIL,
                retryable=True,
            ) from exc
        except ValueError as exc:
            self._attach_llm_trace(trace)
            trace = self._compact_trace(trace)
            if run is not None:
                self._finish_run(
                    run,
                    "FAILED",
                    trace=trace,
                    error={"code": "BAD_REQUEST", "message": str(exc), "retryable": False},
                )
            raise ProblemDetailException(
                status=400,
                code="BAD_REQUEST",
                detail=str(exc) or "Bad request",
                retryable=False,
            ) from exc
        except Exception as exc:
            self._attach_llm_trace(trace)
            trace = self._compact_trace(trace)
            if run is not None:
                self._finish_run(
                    run,
                    "FAILED",
                    trace=trace,
                    error={"code": "AGENT_EXECUTION_FAILED", "message": str(exc), "retryable": False},
                )
            raise ProblemDetailException(
                status=500,
                code="AGENT_EXECUTION_FAILED",
                detail=AGENT_EXECUTION_DETAIL,
                retryable=False,
            ) from exc

    def classify_intent(self, message: str) -> AgentIntent:
        return self.classify_intent_decision(message).intent

    def classify_intent_decision(self, message: str, session_id: str | None = None) -> AgentRouteDecision:
        parsed = self.llm.generate_json_required(
            self._intent_prompt(message, session_id),
            system="You are an intent router. Return strict JSON only.",
            temperature=0.0,
            label="intent_classifier",
        )
        return route_decision_from_llm(parsed, self.llm.max_attempts)

    # --- Wrapper methods to maintain backwards compatibility for mocks and tests ---

    def _validate_user_message(self, message: str) -> str:
        return validate_user_message(message)

    def _ensure_session(self, session_id: str | None, user_id: str | None) -> str:
        return ensure_session(self.db, session_id, user_id)

    def _start_run(self, session_id: str, user_id: str | None) -> AgentRun:
        return start_run(self.db, session_id, user_id)

    def _finish_run(self, run: AgentRun, status: str, trace: dict | None = None, error: dict | None = None) -> None:
        finish_run(self.db, run, status, trace, error)

    def _save_message(
        self,
        session_id: str,
        role: str,
        content: str,
        intent: str | None = None,
        tool_calls: list[ToolCallInfo] | None = None,
        citations=None,
    ) -> None:
        save_message(self.db, session_id, role, content, intent, tool_calls, citations)

    def _recent_messages(self, session_id: str, limit: int = 8) -> list[dict[str, str]]:
        return get_recent_messages(self.db, session_id, limit)

    def _load_session_state(self, session_id: str) -> dict[str, Any]:
        return self.state_store.load(session_id)

    def _save_session_state(self, session_id: str, state: dict[str, Any]) -> None:
        self.state_store.save(session_id, state)

    def _validate_route_decision(self, decision: AgentRouteDecision) -> AgentRouteDecision:
        return validate_route_decision(decision)

    def _resolve_confirmation_via_llm(self, message: str, state: dict[str, Any], session_id: str) -> bool:
        proposal_id = extract_proposal_id(message) or (state.get("activeProposal") or {}).get("proposalId") or ""
        memory = self._recent_messages(session_id)
        try:
            result = self.llm.generate_json_required(
                confirmation_prompt(message, str(proposal_id), memory),
                system="You are a confirmation verifier. Return strict JSON only.",
                temperature=0.0,
                label="confirmation_verifier",
            )
            return bool(result.get("confirmed", False))
        except Exception:
            return False

    def _prepare_commit_route(self, decision: AgentRouteDecision, message: str, state: dict[str, Any], confirmation: bool) -> AgentRouteDecision:
        return prepare_commit_route(decision, message, state, confirmation)

    def _apply_actionability_gate(self, decision: AgentRouteDecision, message: str, state: dict[str, Any], session_id: str) -> AgentRouteDecision:
        memory = self._recent_messages(session_id)
        try:
            gate_result = self.llm.generate_json_required(
                actionability_prompt(message, decision.intent, memory, state),
                system="You are a pre-execution guard. Return strict JSON only.",
                temperature=0.0,
                label="actionability_gate",
            )
        except Exception:
            return decision
        return apply_actionability_gate(decision, gate_result)

    def _direct_route_response(self, session_id: str, run_id: str, message: str, decision: AgentRouteDecision) -> AgentChatResponse:
        return direct_route_response(self.llm, session_id, run_id, message, decision)

    def _attach_llm_trace(self, trace: dict[str, Any]) -> None:
        retry_events = [event.__dict__ for event in getattr(self.llm, "retry_events", [])]
        llm_calls = list(getattr(self.llm, "llm_calls", []))
        if retry_events:
            trace["retryEvents"] = retry_events
        else:
            trace.pop("retryEvents", None)
        if llm_calls:
            trace["llmCalls"] = llm_calls
        else:
            trace.pop("llmCalls", None)

    def _compact_trace(self, trace: dict[str, Any]) -> dict[str, Any]:
        compact = dict(trace or {})
        for key in ("retryEvents", "validation", "responseIds", "agentSteps"):
            if compact.get(key) == []:
                compact.pop(key, None)
        for key in ("routerDecision",):
            if isinstance(compact.get(key), dict):
                compact[key] = {item_key: item_value for item_key, item_value in compact[key].items() if item_value is not None}
        return compact

    def _state_after_direct_response(self, state: dict[str, Any], decision: AgentRouteDecision) -> dict[str, Any]:
        return state_after_direct_response(state, decision)

    def _state_after_tool_calls(self, state: dict[str, Any], decision: AgentRouteDecision, tool_calls: list[ToolCallInfo]) -> dict[str, Any]:
        return state_after_tool_calls(state, decision, tool_calls)

    def _generate_suggested_actions(
        self,
        message: str,
        intent: AgentIntent,
        answer: str,
        tool_calls: list[ToolCallInfo],
        active_proposal: dict[str, Any] | None = None,
    ) -> list[SuggestedAction]:
        return generate_suggested_actions(self.llm, message, intent, answer, tool_calls, active_proposal=active_proposal)

    def _intent_prompt(self, message: str, session_id: str | None = None) -> str:
        memory = self._recent_messages(session_id) if session_id else []
        return intent_prompt(message, memory)

    def _build_agent_response(
        self,
        message: str,
        session_id: str,
        run_id: str,
        intent: AgentIntent,
        answer: str,
        tool_calls: list[ToolCallInfo],
        debug: bool = False,
        trace: dict[str, Any] | None = None,
    ) -> AgentChatResponse:
        from app.agent.schemas import Citation, LearningPathProposalResponse

        citations = []
        proposal = None

        for tool_call in tool_calls:
            citations.extend([Citation(**citation) for citation in tool_call.output.get("citations") or []])
            if "proposal" in tool_call.output:
                proposal = LearningPathProposalResponse(**tool_call.output["proposal"])

        answer, parsed_proposal = self._extract_answer_and_proposal(answer)
        context_output = next(
            (tool_call.output for tool_call in tool_calls if tool_call.name == "learning_path_retrieve_context" and not tool_call.error),
            None,
        )
        if parsed_proposal:
            try:
                proposal = LearningPathProposalService(self.db, llm_client=self.llm).save_proposal_from_llm(session_id, parsed_proposal, context_output)
            except ValueError:
                proposal = None

        suggested_actions = self._generate_suggested_actions(
            message,
            intent,
            answer,
            tool_calls,
            active_proposal=proposal.model_dump() if proposal else None,
        )

        return AgentChatResponse(
            runId=run_id,
            sessionId=session_id,
            intent=intent,
            answer=answer,
            citations=citations,
            proposal=proposal,
            suggestedActions=suggested_actions,
            toolCalls=tool_calls,
            trace=trace if debug else None,
        )

    def _extract_answer_and_proposal(self, answer: str) -> tuple[str, dict[str, Any] | None]:
        text = str(answer or "").strip()
        if not text:
            return answer, None

        parsed = None

        # 1. Try direct parsing
        try:
            parsed = json.loads(text)
        except json.JSONDecodeError:
            pass

        # 2. Try extracting from markdown code block(s)
        if parsed is None:
            import re
            code_blocks = re.findall(r"```(?:json)?\s*(.*?)\s*```", text, re.DOTALL)
            for block in code_blocks:
                try:
                    p = json.loads(block.strip())
                    if isinstance(p, dict):
                        parsed = p
                        break
                except json.JSONDecodeError:
                    pass

        # 3. Fallback to extracting using _extract_json (curly braces matching)
        if parsed is None:
            from app.services.llm_client import _extract_json
            try:
                parsed = json.loads(_extract_json(text))
            except json.JSONDecodeError:
                pass

        if parsed is None or not isinstance(parsed, dict):
            return answer, None

        proposal = parsed.get("proposal")
        if not isinstance(proposal, dict):
            return str(parsed.get("answer") or answer), None
        return str(parsed.get("answer") or answer), proposal


    def stream_answer(self, response: AgentChatResponse):
        if response.intent == "FAILED":
            yield response.answer
            return
        for index in range(0, len(response.answer), 32):
            yield response.answer[index:index + 32]

    def _stream_executor_event(self, raw_event: dict[str, Any], stream: "_StreamContext") -> Iterator[dict[str, Any]]:
        event = raw_event.get("event")
        data = raw_event.get("data") or {}
        if event == "tool_start":
            tool_name = data.get("name")
            part = stream.ensure_tool_part(str(tool_name), status="loading")
            if part:
                yield stream.event("part_created", part)
            yield stream.event("tool_started", {"name": tool_name, "input": data.get("input") or {}})
            return

        if event == "tool_result":
            tool_name = str(data.get("name") or "")
            output = data.get("output") or {}
            error = data.get("error")
            yield stream.event("tool_completed", {"name": tool_name, "output": output, "error": error})
            update = stream.tool_part_update(tool_name, output, error)
            if update:
                yield stream.event("part_updated", update)
                yield stream.event("part_done", {"partId": update["partId"]})
            return

    def _stream_completed_response(
        self,
        response: AgentChatResponse,
        stream: "_StreamContext",
        debug: bool = False,
    ) -> Iterator[dict[str, Any]]:
        for citation in response.citations:
            yield stream.event("citation_added", citation.model_dump())
        if response.intent == "FAILED":
            yield stream.event("error", {"error": response.error.model_dump() if response.error else None})
            return
        if response.proposal:
            current = stream.current_part("learning_path")
            if not current or current.get("status") != "completed":
                part = stream.ensure_part("learning_path", status="loading")
                if part:
                    yield stream.event("part_created", part)
                update = stream.update_part(
                    "learning_path",
                    {
                        "status": "completed",
                        "proposal": response.proposal.model_dump(),
                    },
                )
                yield stream.event("part_updated", update)
                yield stream.event("part_done", {"partId": update["partId"]})
        elif stream.part_keys.get("learning_path"):
            current = stream.current_part("learning_path")
            if current and current.get("status") == "loading":
                update = stream.update_part("learning_path", {"status": "error"})
                yield stream.event("part_updated", update)
                yield stream.event("part_done", {"partId": update["partId"]})

        text_part = stream.create_part("text", status="streaming")
        yield stream.event("part_created", text_part)
        for delta in self.stream_answer(response):
            yield stream.event("part_delta", {"partId": text_part["partId"], "delta": delta})
        yield stream.event("part_done", {"partId": text_part["partId"]})

        if response.suggestedActions:
            actions_part = stream.create_part("suggested_actions", status="completed")
            actions = [action.model_dump() for action in response.suggestedActions]
            actions_part["actions"] = actions
            stream.parts[actions_part["partId"]]["actions"] = actions
            yield stream.event("part_created", actions_part)
            yield stream.event("part_done", {"partId": actions_part["partId"]})

        yield stream.event("final", self._final_message(response, stream, debug=debug))

    def _final_message(self, response: AgentChatResponse, stream: "_StreamContext", debug: bool = False) -> dict[str, Any]:
        message = {
            "messageId": stream.message_id,
            "content": response.answer,
            "parts": list(stream.parts.values()),
            "citations": [citation.model_dump() for citation in response.citations],
            "suggestedReplies": [],
            "actions": [action.model_dump() for action in response.suggestedActions],
        }
        payload = {
            "message": message,
            "intent": response.intent,
        }
        if debug and response.trace:
            payload["trace"] = response.trace
        return payload


def _stream_event(event: str, data: dict[str, Any]) -> dict[str, Any]:
    return {"event": event, "data": data}


TOOL_PARTS = {
    "course_search": ("course_list", "agent.parts.courseList.title"),
    "policy_retrieve": ("policy_result", "agent.parts.policyResult.title"),
    "learning_path_retrieve_context": ("learning_path", "agent.parts.learningPath.title"),
    "learning_path_update_proposal": ("learning_path", "agent.parts.learningPath.title"),
    "learning_path_create": ("action_result", "agent.parts.actionResult.title"),
}


class _StreamContext:
    def __init__(self, run_id: str, session_id: str):
        self.run_id = run_id
        self.session_id = session_id
        self.message_id = f"msg_{uuid4().hex}"
        self.seq = 0
        self.parts: dict[str, dict[str, Any]] = {}
        self.part_keys: dict[str, str] = {}
        self.part_counts: dict[str, int] = {}

    def event(self, event_type: str, data: dict[str, Any]) -> dict[str, Any]:
        self.seq += 1
        payload = {
            "type": event_type,
            "messageId": self.message_id,
            "runId": self.run_id,
            "sessionId": self.session_id,
            "seq": self.seq,
            "timestamp": datetime.utcnow().isoformat(),
            "data": data,
        }
        return {"event": event_type, "data": payload}

    def create_part(self, part_type: str, status: str, title_key: str | None = None) -> dict[str, Any]:
        count = self.part_counts.get(part_type, 0) + 1
        self.part_counts[part_type] = count
        part_id = f"part_{part_type}_{count:03d}"
        part = {
            "partId": part_id,
            "partType": part_type,
            "status": status,
        }
        if title_key:
            part["titleKey"] = title_key
        self.parts[part_id] = part
        return part.copy()

    def ensure_part(self, part_type: str, status: str = "loading", title_key: str | None = None) -> dict[str, Any] | None:
        existing = self.part_keys.get(part_type)
        if existing:
            return None
        part = self.create_part(part_type, status, title_key)
        self.part_keys[part_type] = part["partId"]
        return part

    def update_part(self, part_type: str, data: dict[str, Any]) -> dict[str, Any]:
        part_id = self.part_keys.get(part_type)
        if not part_id:
            part = self.create_part(part_type, data.get("status") or "completed")
            self.part_keys[part_type] = part["partId"]
            part_id = part["partId"]
        self.parts[part_id].update(data)
        return {"partId": part_id, **data}

    def current_part(self, part_type: str) -> dict[str, Any] | None:
        part_id = self.part_keys.get(part_type)
        if not part_id:
            return None
        return self.parts.get(part_id)

    def ensure_tool_part(self, tool_name: str, status: str) -> dict[str, Any] | None:
        spec = TOOL_PARTS.get(tool_name)
        if not spec:
            return None
        part_type, title_key = spec
        return self.ensure_part(part_type, status=status, title_key=title_key)

    def tool_part_update(self, tool_name: str, output: dict[str, Any], error: str | None) -> dict[str, Any] | None:
        spec = TOOL_PARTS.get(tool_name)
        if not spec:
            return None
        part_type, _ = spec
        if tool_name == "learning_path_retrieve_context" and not error:
            candidates = output.get("candidateCourses") or []
            if candidates:
                return None
            return self.update_part(part_type, {"status": "empty", "items": []})
        if error:
            return self.update_part(part_type, {"status": "error", "error": error})
        if tool_name == "course_search":
            items = output.get("results") or []
            return self.update_part(part_type, {"status": "completed" if items else "empty", "items": items})
        if tool_name == "policy_retrieve":
            items = output.get("contexts") or []
            return self.update_part(part_type, {"status": "completed" if items else "empty", "items": items})
        if tool_name == "learning_path_update_proposal" and output.get("proposal"):
            return self.update_part(part_type, {"status": "completed", "proposal": output["proposal"]})
        if tool_name == "learning_path_create":
            return self.update_part(part_type, {"status": "completed", "result": output})
        return None
