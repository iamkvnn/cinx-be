import json
from app.core.database import SessionLocal
from app.models.events import CourseEvent, UserPreferenceEvent, CourseInteractionEvent, EnrolledCourseEvent, WishlistEvent
from app.services.sync_service import SyncService


async def process_message(message_body: bytes, routing_key: str):
    raw = json.loads(message_body.decode("utf-8"))
    print(f"Received message with routing key: {routing_key}, payload: {raw}")
    db = SessionLocal()
    try:
        sync_service = SyncService(db)
        
        if routing_key.startswith("course."):
            event = CourseEvent(**raw)
            sync_service.handle_course_upsert(event.course)
        
        elif routing_key == "enrollment.enrollment.created":
            event = EnrolledCourseEvent(**raw)
            sync_service.handle_enrollment(event.userId, event.courseId)
            
        elif routing_key.startswith("social.wishlist."):
            event = WishlistEvent(**raw)
            sync_service.handle_wishlist(event.userId, event.courseId, event.added)

    finally:
        db.close()