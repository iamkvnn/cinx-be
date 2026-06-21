import json
import logging
import time
from dataclasses import dataclass
from typing import Any, Callable

from app.core.config import settings


logger = logging.getLogger(__name__)


LANGUAGE_INSTRUCTION = (
    "Detect the user's language from the latest user message. "
    "Respond in that same language (if a JSON response is required, output ONLY the raw JSON object matching the requested schema with no preamble or postamble, and place all user-facing strings inside the JSON in the user's language). "
    "Write every user-facing string (answer, question, label) in that same language. "
    "Keep reasoning and internal fields (reason, missingFields) in English. "
    "Keep tool names, IDs, enum values, and JSON keys unchanged."
)


class LLMRetryError(Exception):
    def __init__(self, message: str, attempts: int):
        super().__init__(message)
        self.attempts = attempts


@dataclass
class RetryEvent:
    attempt: int
    max_attempts: int
    retry_after_seconds: int
    error: str


class DigitalOceanLLMClient:
    def __init__(
        self,
        max_attempts: int = 3,
        retry_after_seconds: int = 10,
        sleeper: Callable[[int], None] = time.sleep,
    ):
        self.api_key = settings.DIGITALOCEAN_MODEL_ACCESS_KEY
        self.base_url = settings.DIGITALOCEAN_INFERENCE_BASE_URL.rstrip("/")
        self.model = settings.DIGITALOCEAN_LLM_MODEL
        self.timeout = settings.DIGITALOCEAN_LLM_TIMEOUT_SECONDS
        self.max_output_tokens = settings.DIGITALOCEAN_LLM_MAX_COMPLETION_TOKENS
        self.max_attempts = max_attempts
        self.retry_after_seconds = retry_after_seconds
        self.sleeper = sleeper
        self.retry_events: list[RetryEvent] = []
        self.llm_calls: list[dict[str, Any]] = []
        self._client = None

    def is_configured(self) -> bool:
        return bool(self.api_key)

    def clear_retry_events(self) -> None:
        self.retry_events.clear()

    def clear_llm_calls(self) -> None:
        self.llm_calls.clear()

    def generate_text_required(
        self,
        prompt: str,
        system: str,
        temperature: float = 0.0,
        label: str | None = None,
    ) -> str:
        last_error = "Unknown LLM error."
        started = time.perf_counter()
        response = None
        content = None
        attempts = 0
        for attempt in range(1, self.max_attempts + 1):
            attempts = attempt
            text, error, response = self._generate_text_for_required(
                prompt,
                system=system,
                temperature=temperature,
            )
            if text and not error:
                content = text
                self._record_llm_call(
                    label,
                    started,
                    prompt,
                    system,
                    content,
                    response,
                    retry_count=attempt - 1,
                )
                return text
            last_error = error or "Empty LLM response."
            self._record_retry_or_sleep(attempt, last_error)
        self._record_llm_call(
            label,
            started,
            prompt,
            system,
            content,
            response,
            retry_count=max(0, attempts - 1),
            error=last_error,
        )
        raise LLMRetryError(last_error, self.max_attempts)

    def generate_json_required(
        self,
        prompt: str,
        system: str,
        temperature: float = 0.0,
        label: str | None = None,
    ) -> dict:
        last_error = "Invalid JSON response."
        started = time.perf_counter()
        response = None
        content = None
        attempts = 0
        for attempt in range(1, self.max_attempts + 1):
            attempts = attempt
            text, error, response = self._generate_text_for_required(
                prompt,
                system=system,
                temperature=temperature,
            )
            if text and not error:
                content = text
                try:
                    parsed = json.loads(_extract_json(text))
                    self._record_llm_call(
                        label,
                        started,
                        prompt,
                        system,
                        content,
                        response,
                        retry_count=attempt - 1,
                    )
                    return parsed
                except json.JSONDecodeError as exc:
                    last_error = f"Invalid JSON response: {exc}"
            else:
                last_error = error or "Empty LLM response."
            self._record_retry_or_sleep(attempt, last_error)
        self._record_llm_call(
            label,
            started,
            prompt,
            system,
            content,
            response,
            retry_count=max(0, attempts - 1),
            error=last_error,
        )
        raise LLMRetryError(last_error, self.max_attempts)

    def generate_json_array_required(
        self,
        prompt: str,
        system: str,
        temperature: float = 0.0,
        label: str | None = None,
    ) -> list:
        last_error = "Invalid JSON array response."
        started = time.perf_counter()
        response = None
        content = None
        attempts = 0
        for attempt in range(1, self.max_attempts + 1):
            attempts = attempt
            text, error, response = self._generate_text_for_required(
                prompt,
                system=system,
                temperature=temperature,
            )
            if text and not error:
                content = text
                try:
                    parsed = json.loads(_extract_json_array(text))
                    if isinstance(parsed, list):
                        self._record_llm_call(
                            label,
                            started,
                            prompt,
                            system,
                            content,
                            response,
                            retry_count=attempt - 1,
                        )
                        return parsed
                    last_error = "JSON response was not an array."
                except json.JSONDecodeError as exc:
                    last_error = f"Invalid JSON array response: {exc}"
            else:
                last_error = error or "Empty LLM response."
            self._record_retry_or_sleep(attempt, last_error)
        self._record_llm_call(
            label,
            started,
            prompt,
            system,
            content,
            response,
            retry_count=max(0, attempts - 1),
            error=last_error,
        )
        raise LLMRetryError(last_error, self.max_attempts)

    def generate_text(
        self,
        prompt: str,
        system: str = "You are a helpful assistant.",
        temperature: float = 0.2,
    ) -> tuple[str | None, str | None]:
        text, error, _response = self._generate_text_once(prompt, system=system, temperature=temperature)
        return text, error

    def _generate_text_once(
        self,
        prompt: str,
        system: str = "You are a helpful assistant.",
        temperature: float = 0.2,
    ):
        if not self.is_configured():
            return None, "DigitalOcean model access key not configured.", None

        try:
            response = self.create_response(
                input_items=[{"role": "user", "content": prompt}],
                instructions=system,
                temperature=temperature,
            )
        except Exception as exc:
            if exc.__class__.__module__.startswith("openai"):
                status_code = getattr(exc, "status_code", None)
                if status_code:
                    logger.warning(
                        "DigitalOcean LLM request failed status=%s error=%s",
                        status_code,
                        exc,
                    )
                    return None, f"DigitalOcean LLM request failed with status {status_code}.", None
                logger.warning("DigitalOcean LLM request failed: %s", exc)
                return None, "DigitalOcean LLM request failed.", None
            raise

        try:
            content = response.output_text
        except (ValueError, KeyError, IndexError, TypeError, AttributeError) as exc:
            logger.warning("DigitalOcean LLM response could not be processed: %s", exc)
            return None, "DigitalOcean LLM response could not be processed.", response

        return content, None, response

    def _generate_text_for_required(
        self,
        prompt: str,
        system: str,
        temperature: float,
    ):
        current = getattr(self, "generate_text")
        if getattr(current, "__func__", None) is not DigitalOceanLLMClient.generate_text:
            text, error = current(prompt, system=system, temperature=temperature)
            return text, error, None
        return self._generate_text_once(prompt, system=system, temperature=temperature)

    def create_response(
        self,
        input_items: list[dict],
        instructions: str,
        tools: list[dict] | None = None,
        tool_choice: str | dict | None = None,
        temperature: float = 0.2,
        parallel_tool_calls: bool = False,
        label: str | None = None,
    ):
        started = time.perf_counter()
        kwargs = {
            "model": self.model,
            "instructions": self._with_language_instruction(instructions),
            "input": input_items,
            "temperature": temperature,
            "max_output_tokens": self.max_output_tokens,
            "parallel_tool_calls": parallel_tool_calls,
        }
        if tools is not None:
            kwargs["tools"] = tools
        if tool_choice is not None:
            kwargs["tool_choice"] = tool_choice
        try:
            response = self._get_client().responses.create(**kwargs)
        except Exception as exc:
            self._record_llm_call(
                label,
                started,
                input_items,
                instructions,
                "",
                None,
                retry_count=0,
                error=str(exc),
            )
            raise
        output_text = getattr(response, "output_text", "") or ""
        self._record_llm_call(label, started, input_items, instructions, output_text, response, retry_count=0)
        return response

    def stream_text(
        self,
        prompt: str,
        system: str = "You are a helpful assistant.",
        temperature: float = 0.2,
    ):
        if not self.is_configured():
            yield "DigitalOcean model access key not configured."
            return

        try:
            stream = self._get_client().chat.completions.create(
                model=self.model,
                messages=[
                    {"role": "system", "content": system},
                    {"role": "user", "content": prompt},
                ],
                temperature=temperature,
                max_completion_tokens=self.max_output_tokens,
                stream=True,
            )
            for event in stream:
                delta = event.choices[0].delta
                content = getattr(delta, "content", None)
                if content:
                    yield content
        except Exception as exc:
            if exc.__class__.__module__.startswith("openai"):
                logger.warning("DigitalOcean LLM streaming request failed: %s", exc)
                yield "DigitalOcean LLM streaming request failed."
                return
            raise

    def _get_client(self):
        if self._client is not None:
            return self._client

        try:
            from openai import OpenAI
        except ImportError as exc:
            raise RuntimeError("openai is not installed") from exc

        self._client = OpenAI(
            base_url=self.base_url,
            api_key=self.api_key,
            timeout=self.timeout,
        )
        return self._client

    def _record_retry_or_sleep(self, attempt: int, error: str) -> None:
        if attempt >= self.max_attempts:
            return
        self.retry_events.append(
            RetryEvent(
                attempt=attempt,
                max_attempts=self.max_attempts,
                retry_after_seconds=self.retry_after_seconds,
                error=error,
            )
        )
        self.sleeper(self.retry_after_seconds)

    def _with_language_instruction(self, system: str) -> str:
        return f"{system}\n\n{LANGUAGE_INSTRUCTION}"

    def _record_llm_call(
        self,
        label: str | None,
        started: float,
        input_value,
        system: str,
        output_text: str | None,
        response,
        retry_count: int = 0,
        error: str | None = None,
    ) -> None:
        if not label:
            return
        self.llm_calls.append(
            {
                "label": label,
                "durationMs": round((time.perf_counter() - started) * 1000, 2),
                "model": self.model,
                "inputCharCount": _char_count(input_value) + _char_count(system),
                "outputCharCount": _char_count(output_text),
                "tokenUsage": _usage_dict(response),
                "retryCount": retry_count,
                "error": error,
            }
        )


def _extract_json(text: str) -> str:
    cleaned = text.replace("```json", "").replace("```", "").strip()
    if cleaned.startswith("{") and cleaned.endswith("}"):
        return cleaned
    start = cleaned.find("{")
    end = cleaned.rfind("}")
    if start != -1 and end != -1 and end > start:
        return cleaned[start:end + 1]
    return cleaned


def _extract_json_array(text: str) -> str:
    cleaned = text.replace("```json", "").replace("```", "").strip()
    if cleaned.startswith("[") and cleaned.endswith("]"):
        return cleaned
    start = cleaned.find("[")
    end = cleaned.rfind("]")
    if start != -1 and end != -1 and end > start:
        return cleaned[start:end + 1]
    return cleaned


def _char_count(value) -> int:
    if value is None:
        return 0
    if isinstance(value, str):
        return len(value)
    return len(json.dumps(value, ensure_ascii=False, default=str))


def _usage_dict(response) -> dict | None:
    usage = getattr(response, "usage", None) if response is not None else None
    if usage is None:
        return None
    if isinstance(usage, dict):
        return dict(usage)
    result = {}
    for key in ("input_tokens", "output_tokens", "total_tokens", "prompt_tokens", "completion_tokens"):
        value = getattr(usage, key, None)
        if value is not None:
            result[key] = value
    return result or None
