import json
import urllib.error
import urllib.request

from app.core.config import settings


class LearningPathCommitClient:
    def create_path(self, payload: dict, token: str | None) -> dict:
        if not token:
            return {
                "success": False,
                "message": "Bearer token is required to create a learning path.",
                "data": None,
            }

        url = f"{settings.LEARNING_SERVICE_BASE_URL.rstrip('/')}/api/v1/learning-paths"
        body = json.dumps(payload).encode("utf-8")
        request = urllib.request.Request(
            url,
            data=body,
            method="POST",
            headers={
                "Authorization": f"Bearer {token}",
                "Content-Type": "application/json",
                "Accept": "application/json",
            },
        )
        try:
            with urllib.request.urlopen(request, timeout=20) as response:
                return json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as exc:
            detail = exc.read().decode("utf-8")
            return {
                "success": False,
                "message": detail or f"Learning service returned status {exc.code}.",
                "data": None,
            }
        except urllib.error.URLError as exc:
            return {
                "success": False,
                "message": f"Could not reach learning service: {exc.reason}",
                "data": None,
            }

    def commit(self, payload: dict, token: str | None) -> dict:
        return self.create_path(payload, token)
