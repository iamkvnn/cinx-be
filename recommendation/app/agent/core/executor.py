import json
from datetime import datetime
from typing import Any, Callable, Generator
from sqlalchemy.orm import Session

from app.agent.schemas import ToolCallInfo, AgentIntent
from app.agent.core.router import AgentRouteDecision
from app.agent.core.session import public_state_context
from app.agent.core.tools import ToolRegistry
from app.agent.core.output import GroundingValidator, contains_raw_tool_syntax
from app.agent.core.prompts import agent_instructions, repair_prompt
from app.entities.agent_session import AgentRun
from app.services.llm_client import DigitalOceanLLMClient, LLMRetryError

MAX_AGENT_STEPS = 6


class AgentExecutor:
    def __init__(
        self,
        db: Session,
        llm: DigitalOceanLLMClient,
        registry: ToolRegistry,
        session_id: str,
        intent: AgentIntent,
        decision: AgentRouteDecision,
        state: dict[str, Any],
        run: AgentRun,
        trace: dict[str, Any],
        recent_messages_fn: Callable[[str], list[dict[str, str]]],
        grounding_validator: GroundingValidator,
    ):
        self.db = db
        self.llm = llm
        self.registry = registry
        self.session_id = session_id
        self.intent = intent
        self.decision = decision
        self.state = state
        self.agent_run = run
        self.trace = trace
        self.recent_messages_fn = recent_messages_fn
        self.grounding = grounding_validator

    def run_stream(self) -> Generator[dict[str, Any], None, tuple[list[ToolCallInfo], str, list[str], list[dict[str, Any]]]]:
        """Runs the streaming tool execution loop, yielding sse-friendly stream events."""
        input_items = self._build_manual_input()
        tool_calls: list[ToolCallInfo] = []
        response_ids: list[str] = []
        self.trace["agentSteps"] = []
        allowed_tools = set(self.decision.allowedTools)

        for step in range(1, MAX_AGENT_STEPS + 1):
            if not tool_calls and allowed_tools:
                tool_choice = "required"
            elif step < MAX_AGENT_STEPS and allowed_tools:
                tool_choice = "auto"
            else:
                tool_choice = "none"

            if tool_calls:
                yield self._stream_event("thinking", {"stage": "finalizing", "step": step, "runId": self.agent_run.id, "sessionId": self.session_id})

            response = self._create_llm_response(input_items, allowed_tools, tool_choice)
            
            response_id = getattr(response, "id", None)
            if response_id:
                response_ids.append(response_id)
            
            function_calls = self._function_calls(response)
            self.trace["agentSteps"].append(
                {
                    "step": step,
                    "responseId": response_id,
                    "functionCallCount": len(function_calls),
                    "toolChoice": tool_choice,
                }
            )

            if not function_calls:
                answer = self._get_final_answer(response, tool_choice, tool_calls)
                answer = self._repair_grounding_if_needed(self._latest_user_message(), answer, tool_calls)
                return tool_calls, answer, response_ids, input_items

            self._finish_run_status("TOOL_RUNNING")
            for call in function_calls:
                tool_name = str(call.get("name") or "")
                call_id = str(call.get("call_id") or call.get("id") or f"call-{step}-{len(tool_calls)}")
                arguments_text = call.get("arguments") or "{}"
                
                tool_input, tool_output, tool_call = self._prepare_and_validate_tool(tool_name, call_id, arguments_text, allowed_tools)
                
                if tool_call.error:
                    tool_calls.append(tool_call)
                    self._append_input_items(input_items, call_id, tool_name, tool_call)
                    yield self._stream_event("tool_start", {"name": tool_name, "input": tool_call.input})
                    yield self._stream_event("tool_result", {"name": tool_name, "output": tool_call.output, "error": tool_call.error})
                    continue

                yield self._stream_event(
                    "thinking",
                    {"stage": "tool_running", "toolName": tool_name, "runId": self.agent_run.id, "sessionId": self.session_id},
                )
                yield self._stream_event("tool_start", {"name": tool_name, "input": tool_input})
                
                tool_output, tool_call = self._execute_tool(tool_name, tool_input)
                tool_calls.append(tool_call)
                self._append_input_items(input_items, call_id, tool_name, tool_call)
                
                yield self._stream_event("tool_result", {"name": tool_name, "output": tool_call.output, "error": tool_call.error})

        raise RuntimeError(f"Agent exceeded max tool steps ({MAX_AGENT_STEPS}).")

    def _build_manual_input(self) -> list[dict[str, Any]]:
        items = [
            {"role": message["role"], "content": message["content"]}
            for message in self.recent_messages_fn(self.session_id)
            if message["role"] in {"user", "assistant"}
        ]
        if self.state:
            items.append(
                {
                    "role": "user",
                    "content": "Session state for resolving references: "
                    + json.dumps(public_state_context(self.state), ensure_ascii=False),
                }
            )
        return items

    def _create_llm_response(self, input_items, allowed_tools, tool_choice):
        return self.llm.create_response(
            input_items=input_items,
            instructions=agent_instructions(self.intent, self.decision, self.state),
            tools=self.registry.openai_tools(allowed_tools),
            tool_choice=tool_choice,
            temperature=0.2,
            parallel_tool_calls=False,
            label="agent_tool_or_final_response",
        )

    def _function_calls(self, response) -> list[dict[str, Any]]:
        calls = []
        for item in getattr(response, "output", []) or []:
            data = item if isinstance(item, dict) else item.__dict__
            if data.get("type") == "function_call":
                calls.append(
                    {
                        "id": data.get("id"),
                        "call_id": data.get("call_id"),
                        "name": data.get("name"),
                        "arguments": data.get("arguments"),
                    }
                )
        return calls

    def _get_final_answer(self, response, tool_choice, tool_calls) -> str:
        if tool_choice == "required":
            raise LLMRetryError("Agent did not call the required tool.", self.llm.max_attempts)
        answer = getattr(response, "output_text", "") or ""
        if not answer.strip():
            raise LLMRetryError("Empty agent response.", self.llm.max_attempts)
        return answer

    def _prepare_and_validate_tool(self, tool_name, call_id, arguments_text, allowed_tools):
        try:
            tool_input = json.loads(arguments_text) if isinstance(arguments_text, str) else arguments_text
        except json.JSONDecodeError as exc:
            tool_input = {}
            tool_output = {"success": False, "error": f"Invalid tool arguments JSON: {exc}"}
            tool_call = ToolCallInfo(name=tool_name, input=tool_input, output=tool_output, error=tool_output["error"])
            return tool_input, tool_output, tool_call

        if tool_name not in allowed_tools:
            tool_output = {"success": False, "error": f"Tool {tool_name} is not allowed for {self.decision.nextFlow}."}
            tool_call = ToolCallInfo(name=tool_name, input=tool_input, output=tool_output, error=tool_output["error"])
            self.trace.setdefault("validation", []).append(
                {"tool": tool_name, "ok": False, "reason": tool_output["error"]}
            )
            return tool_input, tool_output, tool_call

        if tool_name == "learning_path_create":
            proposal = self.state.get("activeProposal") or {}
            tool_input.setdefault("proposalId", proposal.get("proposalId"))
            tool_input.setdefault("version", proposal.get("version"))
            tool_input["confirmed"] = True

        return tool_input, {}, ToolCallInfo(name=tool_name, input=tool_input, output={})

    def _execute_tool(self, tool_name, tool_input):
        try:
            tool_output = self.registry.execute(tool_name, tool_input)
            tool_call = ToolCallInfo(name=tool_name, input=tool_input, output=tool_output)
            self.trace.setdefault("validation", []).append({"tool": tool_name, "ok": True})
        except ValueError as exc:
            tool_output = {"success": False, "error": str(exc)}
            tool_call = ToolCallInfo(name=tool_name, input=tool_input, output=tool_output, error=str(exc))
            self.trace.setdefault("validation", []).append({"tool": tool_name, "ok": False, "reason": str(exc)})
        return tool_output, tool_call

    def _append_input_items(self, input_items, call_id, tool_name, tool_call):
        input_items.append(
            {
                "type": "function_call",
                "call_id": call_id,
                "name": tool_name,
                "arguments": json.dumps(tool_call.input, ensure_ascii=False),
            }
        )
        input_items.append(
            {
                "type": "function_call_output",
                "call_id": call_id,
                "output": json.dumps(tool_call.output, ensure_ascii=False),
            }
        )

    def _repair_grounding_if_needed(self, user_message: str, answer: str, tool_calls: list[ToolCallInfo]) -> str:
        combined_output = {"toolOutputs": [tool_call.output for tool_call in tool_calls]}
        ok, reason = self.grounding.validate(answer, combined_output)
        if ok:
            return answer
        repair = self.llm.create_response(
            input_items=[
                {
                    "role": "user",
                    "content": repair_prompt(user_message, self.intent, answer, combined_output, reason),
                }
            ],
            instructions="You are CINX learning assistant. Fix grounding issues and use only the provided tool outputs.",
            tools=self.registry.openai_tools(),
            tool_choice="none",
            temperature=0.0,
            parallel_tool_calls=False,
            label="grounding_repair",
        )
        repaired = getattr(repair, "output_text", "") or ""
        ok, reason = self.grounding.validate(repaired, combined_output)
        if not ok or contains_raw_tool_syntax(repaired):
            raise LLMRetryError(f"Grounding validation failed: {reason}", self.llm.max_attempts)
        return repaired

    def _latest_user_message(self) -> str:
        for message in reversed(self.recent_messages_fn(self.session_id)):
            if message.get("role") == "user":
                return str(message.get("content") or "")
        return self.decision.reason or ""

    def _finish_run_status(self, status: str) -> None:
        self.agent_run.status = status
        self.agent_run.updated_at = datetime.utcnow()
        self.db.commit()

    def _stream_event(self, event: str, data: dict[str, Any]) -> dict[str, Any]:
        return {"event": event, "data": data}
