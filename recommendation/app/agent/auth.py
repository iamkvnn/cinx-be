import base64
import hashlib
import hmac
import json
import time
from dataclasses import dataclass

from fastapi import Header, HTTPException

from app.core.config import settings


@dataclass(frozen=True)
class RequestUser:
    user_id: str | None
    token: str | None


def _b64url_decode(value: str) -> bytes:
    padding = "=" * (-len(value) % 4)
    return base64.urlsafe_b64decode(value + padding)


def _decode_hs512_jwt(token: str) -> dict:
    parts = token.split(".")
    if len(parts) != 3:
        raise ValueError("Malformed JWT")

    signing_input = f"{parts[0]}.{parts[1]}".encode("utf-8")
    signature = _b64url_decode(parts[2])
    expected = hmac.new(settings.JWT_ACCESS_SECRET.encode("utf-8"), signing_input, hashlib.sha512).digest()
    if not hmac.compare_digest(signature, expected):
        raise ValueError("Invalid JWT signature")

    payload = json.loads(_b64url_decode(parts[1]).decode("utf-8"))
    exp = payload.get("exp")
    if exp is not None and int(exp) < int(time.time()):
        raise ValueError("Expired JWT")
    return payload


def current_user(authorization: str | None = Header(default=None)) -> RequestUser:
    token = None
    if authorization and authorization.startswith("Bearer "):
        token = authorization.removeprefix("Bearer ").strip()

    if not token:
        return RequestUser(user_id=None, token=None)

    if not settings.JWT_ACCESS_SECRET:
        return RequestUser(user_id=None, token=token)

    try:
        payload = _decode_hs512_jwt(token)
    except ValueError as exc:
        raise HTTPException(status_code=401, detail=str(exc)) from exc

    return RequestUser(user_id=payload.get("sub") or payload.get("userId"), token=token)
