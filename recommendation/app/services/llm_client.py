import logging

from app.core.config import settings


logger = logging.getLogger(__name__)


class DigitalOceanLLMClient:
    def __init__(self):
        self.api_key = settings.DIGITALOCEAN_MODEL_ACCESS_KEY
        self.base_url = settings.DIGITALOCEAN_INFERENCE_BASE_URL.rstrip("/")
        self.model = settings.DIGITALOCEAN_LLM_MODEL
        self.timeout = settings.DIGITALOCEAN_LLM_TIMEOUT_SECONDS
        self.max_completion_tokens = settings.DIGITALOCEAN_LLM_MAX_COMPLETION_TOKENS
        self._client = None

    def is_configured(self) -> bool:
        return bool(self.api_key)

    def generate_learning_path_json(self, prompt: str) -> tuple[str | None, str | None]:
        if not self.is_configured():
            return None, "DigitalOcean model access key not configured. Cannot generate learning path."

        try:
            response = self._get_client().chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": "You generate strict JSON for learning paths. Return JSON only.",
                    },
                    {"role": "user", "content": prompt},
                ],
                temperature=0.2,
                max_completion_tokens=self.max_completion_tokens,
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
                    return None, f"DigitalOcean LLM request failed with status {status_code}."
                logger.warning("DigitalOcean LLM request failed: %s", exc)
                return None, "DigitalOcean LLM request failed."
            raise

        try:
            content = response.choices[0].message.content
        except (ValueError, KeyError, IndexError, TypeError, AttributeError) as exc:
            logger.warning("DigitalOcean LLM response could not be processed: %s", exc)
            return None, "DigitalOcean LLM response could not be processed."

        return content, None

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
