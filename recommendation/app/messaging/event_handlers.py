import json
from app.core.database import SessionLocal
from app.models.events import CourseEvent, UserPreferenceEvent, CourseInteractionEvent
from app.services.sync_service import SyncService


async def process_message(message_body: bytes):
    raw = json.loads(message_body.decode("utf-8"))
    event_type = raw.get("eventType")

    db = SessionLocal()
    try:
        sync_service = SyncService(db)

        if event_type in {"course.created", "course.updated", "course.published", "course.unpublished"}:
            event = CourseEvent(**raw)
            sync_service.handle_course_upsert(event.payload)

        elif event_type in {"user.preference.selected", "user.preference.updated"}:
            event = UserPreferenceEvent(**raw)
            sync_service.handle_user_preferences(event.payload)

        elif event_type in {"course.viewed", "course.wishlisted", "course.enrolled", "course.completed", "course.rated"}:
            event = CourseInteractionEvent(**raw)
            sync_service.handle_interaction(event.payload)

    finally:
        db.close()