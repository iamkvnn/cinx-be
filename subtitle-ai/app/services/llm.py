import json
import logging
import re
import time
from typing import Any

from app.core.config import settings


logger = logging.getLogger(__name__)


class DigitalOceanLLM:
    def __init__(self):
        self.api_key = settings.DIGITALOCEAN_MODEL_ACCESS_KEY
        self.base_url = settings.DIGITALOCEAN_INFERENCE_BASE_URL.rstrip("/")
        self.model = settings.DIGITALOCEAN_LLM_MODEL
        self._client = None

    def is_configured(self) -> bool:
        return bool(self.api_key)

    def split_long_sentence(self, text: str) -> list[str]:
        prompt = f"""
Split the following sentence into smaller natural subtitle-friendly sentences.

Rules:
- Keep the original meaning.
- Do not summarize.
- Do not add new information.
- Return JSON only.
- Each item must be a string.
- Max 120 characters per item.

Text:
{text}

Output format:
{{"sentences": ["...", "..."]}}
"""
        payload = self._chat_json_with_retries(
            "split_long_sentence",
            "You are a subtitle segmentation assistant. Return valid JSON only.",
            prompt,
            max_tokens=800,
            temperature=0.1,
        )
        chunks = _clean_string_list(payload.get("sentences"), "")
        if not chunks:
            raise ValueError("LLM split_long_sentence response missing chunks")
        return chunks

    def summarize_and_extract_terms(
        self,
        transcript_text: str,
        source_language: str,
        target_language: str,
    ) -> dict[str, Any]:
        prompt = f"""
Analyze this transcript.

Source language: {source_language}
Target language: {target_language}

Tasks:
1. Summarize the content in 0-8 bullet points if any.
2. Extract important terminology.
3. For each term, provide: term, meaning, category, suggested_translation.

The suggested_translation must be in the target language: {target_language}.

Return JSON only.

Transcript:
{transcript_text[:12000]}

Output format:
{{
  "summary": ["...", "..."],
  "terminology": [
    {{
      "term": "...",
      "meaning": "...",
      "category": "...",
      "suggested_translation": "..."
    }}
  ]
}}
"""
        payload = self._chat_json_with_retries(
            "summarize_and_extract_terms",
            "You analyze transcripts and return valid JSON only.",
            prompt,
            max_tokens=2500,
            temperature=0.2,
        )
        if "summary" not in payload or "terminology" not in payload:
            raise ValueError(
                "LLM summarize_and_extract_terms response missing required fields"
            )
        return {
            "summary": payload.get("summary") if isinstance(payload.get("summary"), list) else [],
            "terminology": payload.get("terminology") if isinstance(payload.get("terminology"), list) else [],
        }

    def translate_batch(
        self,
        items: list[dict],
        source_language: str,
        target_language: str,
        analysis: dict[str, Any],
    ) -> list[dict]:
        prompt = f"""
Translate subtitle items from {source_language} to {target_language}.

Rules:
- Preserve item id.
- Do not merge items.
- Do not remove items.
- Use the provided terminology consistently.
- Natural subtitle style, not overly literal.
- Return JSON only.

Summary:
{json.dumps(analysis.get("summary", []), ensure_ascii=False)}

Terminology:
{json.dumps(analysis.get("terminology", []), ensure_ascii=False)}

Items:
{json.dumps(items, ensure_ascii=False)}

Output format:
{{
  "items": [
    {{"id": 1, "translated_text": "..."}},
    {{"id": 2, "translated_text": "..."}}
  ]
}}
"""
        payload = self._chat_json_with_retries(
            "translate_items",
            "You are a professional subtitle translator. Return valid JSON only.",
            prompt,
            max_tokens=3000,
            temperature=0.2,
        )
        translated = payload.get("items")
        if not isinstance(translated, list):
            raise ValueError("LLM translate_items response missing items")
        by_id = {
            str(item.get("id")): str(item.get("translated_text") or item.get("text") or "")
            for item in translated
            if isinstance(item, dict)
        }
        missing_ids = [item["id"] for item in items if str(item["id"]) not in by_id]
        if missing_ids:
            raise ValueError(f"LLM translate_items response missing ids: {missing_ids}")
        return [{"id": item["id"], "text": by_id[str(item["id"])]} for item in items]

    def split_subtitle_batch(self, items: list[dict], target_language: str) -> list[dict]:
        compact_items = [
            {
                "id": item["id"],
                "text": item["text"],
                "duration_seconds": round(float(item.get("duration_seconds", 0)), 2),
            }
            for item in items
        ]
        prompt = f"""
You are a professional subtitle editor.

Task:
Split each translated subtitle text into natural subtitle chunks for {target_language}.

Important:
- Do NOT translate.
- Do NOT summarize.
- Do NOT add new meaning.
- Do NOT create timestamps.
- Preserve each input id.
- Prefer natural phrase boundaries.
- A chunk can contain 1 or 2 lines.
- Each line should be <= {settings.MAX_SUBTITLE_LINE_CHARS} characters if possible.
- Each chunk should be <= {settings.MAX_SUBTITLE_LINE_CHARS * settings.MAX_SUBTITLE_LINES} characters if possible.
- Use "\\n" inside a chunk to represent line breaks.
- If the original text is already short and natural, keep it as one chunk.
- Return valid JSON only.

Input items:
{json.dumps(compact_items, ensure_ascii=False)}

Output format:
{{
  "items": [
    {{"id": 1, "chunks": ["first subtitle chunk", "second subtitle chunk"]}}
  ]
}}
"""
        payload = self._chat_json_with_retries(
            "split_subtitle_batch",
            "You split translated text into readable subtitle chunks and return JSON only.",
            prompt,
            max_tokens=3500,
            temperature=0.1,
        )
        output_items = payload.get("items")
        if not isinstance(output_items, list):
            raise ValueError("LLM split_subtitle_batch response missing items")
        output_ids = {str(item.get("id")) for item in output_items if isinstance(item, dict)}
        missing_ids = [item["id"] for item in items if str(item["id"]) not in output_ids]
        if missing_ids:
            raise ValueError(f"LLM split_subtitle_batch response missing ids: {missing_ids}")
        return output_items

    def _chat_json_with_retries(
        self,
        task: str,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int,
        temperature: float,
    ) -> dict[str, Any]:
        max_retries = max(0, int(settings.DIGITALOCEAN_LLM_MAX_RETRIES))
        attempts = max_retries + 1
        last_error: Exception | None = None
        for attempt in range(1, attempts + 1):
            try:
                return self._chat_json(task, system_prompt, user_prompt, max_tokens, temperature, attempt, attempts)
            except (OpenAIProviderError, ValueError, KeyError, IndexError, TypeError) as exc:
                last_error = exc
                if attempt >= attempts:
                    logger.error(
                        "DigitalOcean LLM failed after retries task=%s attempts=%s error=%s",
                        task,
                        attempts,
                        exc,
                    )
                    raise
                logger.warning(
                    "DigitalOcean LLM attempt failed task=%s attempt=%s/%s retry_delay=%s error=%s",
                    task,
                    attempt,
                    attempts,
                    settings.DIGITALOCEAN_LLM_RETRY_DELAY_SECONDS,
                    exc,
                )
                time.sleep(settings.DIGITALOCEAN_LLM_RETRY_DELAY_SECONDS)
        raise RuntimeError(f"DigitalOcean LLM failed task={task}: {last_error}")

    def _chat_json(
        self,
        task: str,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int,
        temperature: float,
        attempt: int = 1,
        attempts: int = 1,
    ) -> dict[str, Any]:
        if not self.api_key:
            raise RuntimeError("DigitalOcean model access key is not configured")
        request_size = len(user_prompt.encode("utf-8"))
        logger.info(
            "Calling DigitalOcean LLM task=%s model=%s attempt=%s/%s timeout=%s input_bytes=%s item_count=%s",
            task,
            self.model,
            attempt,
            attempts,
            settings.DIGITALOCEAN_LLM_TIMEOUT_SECONDS,
            request_size,
            user_prompt.count('"id"'),
        )
        started_at = time.perf_counter()
        try:
            response = self._get_client().chat.completions.create(
                model=self.model,
                messages=[{"role": "system", "content": system_prompt}, {"role": "user", "content": user_prompt}],
                temperature=temperature,
                max_completion_tokens=min(max_tokens, settings.DIGITALOCEAN_LLM_MAX_COMPLETION_TOKENS),
            )
        except Exception as exc:
            if exc.__class__.__module__.startswith("openai"):
                raise OpenAIProviderError(str(exc)) from exc
            raise
        duration_ms = int((time.perf_counter() - started_at) * 1000)
        content = (response.choices[0].message.content or "").strip()
        parsed = parse_json_object(content)
        if not parsed:
            raise ValueError("DigitalOcean LLM returned empty or invalid JSON")
        logger.info(
            "DigitalOcean LLM completed task=%s attempt=%s/%s status=%s duration_ms=%s output_chars=%s keys=%s",
            task,
            attempt,
            attempts,
            "ok",
            duration_ms,
            len(content),
            sorted(parsed.keys()),
        )
        return parsed

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
            timeout=settings.DIGITALOCEAN_LLM_TIMEOUT_SECONDS,
        )
        return self._client


class OpenAIProviderError(Exception):
    pass


def parse_json_object(content: str) -> dict[str, Any]:
    try:
        parsed = json.loads(content)
        return parsed if isinstance(parsed, dict) else {}
    except json.JSONDecodeError:
        match = re.search(r"\{.*\}", content, re.DOTALL)
        if not match:
            return {}
        try:
            parsed = json.loads(match.group(0))
            return parsed if isinstance(parsed, dict) else {}
        except json.JSONDecodeError:
            logger.warning("LLM returned invalid JSON")
            return {}


def _clean_string_list(value: Any, fallback_text: str) -> list[str]:
    if not isinstance(value, list):
        return [fallback_text.strip()] if fallback_text.strip() else []
    cleaned = [" ".join(str(item).split()) for item in value if str(item).strip()]
    return cleaned or ([fallback_text.strip()] if fallback_text.strip() else [])
