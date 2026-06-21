import json
import logging
from app.core.database import SessionLocal
from app.agent.services.knowledge import KnowledgeService
from app.models.events import CourseEvent, EnrolledCourseEvent, PolicyKnowledgeEvent, UserPreferenceEvent, WishlistEvent
from app.services.sync_service import SyncService
from app.services.rag_index import rebuild_rag_index

logger = logging.getLogger(__name__)


async def process_message(message_body: bytes, routing_key: str):
    raw = json.loads(message_body.decode("utf-8"))
    logger.info("Received RabbitMQ message - routing_key=%s payload=%s", routing_key, raw)
    db = SessionLocal()
    try:
        sync_service = SyncService(db)
        
        if routing_key.startswith("course."):
            event = CourseEvent(**raw)
            sync_service.handle_course_upsert(event.course)
            rebuild_rag_index(db)
            logger.info("Processed course event - routing_key=%s course_id=%s", routing_key, event.course.id)
        
        elif routing_key == "enrollment.enrollment.created":
            event = EnrolledCourseEvent(**raw)
            sync_service.handle_enrollment(event.userId, event.courseId)
            logger.info("Processed enrollment event - user_id=%s course_id=%s", event.userId, event.courseId)
            
        elif routing_key.startswith("social.wishlist."):
            event = WishlistEvent(**raw)
            sync_service.handle_wishlist(event.userId, event.courseId, event.added)
            logger.info(
                "Processed wishlist event - user_id=%s course_id=%s added=%s",
                event.userId,
                event.courseId,
                event.added,
            )

        elif routing_key == "user.preference.updated":
            event = UserPreferenceEvent(**raw)
            sync_service.handle_user_preferences(event.payload)
            logger.info(
                "Processed user preference event - user_id=%s category_count=%s",
                event.payload.userId,
                len(event.payload.categoryIds),
            )

        elif routing_key == "user.policy.published":
            event = PolicyKnowledgeEvent(**raw)
            if not event.title or not event.content:
                raise ValueError("Policy published event requires title and content")
            KnowledgeService(db).replace_document(
                document_id=event.documentId,
                title=event.title,
                content=event.content,
                source_type=event.sourceType,
                source_url=event.sourceUrl,
            )
            logger.info(
                "Processed policy published event - document_id=%s version=%s",
                event.documentId,
                event.versionNumber,
            )

        elif routing_key == "user.policy.archived":
            event = PolicyKnowledgeEvent(**raw)
            KnowledgeService(db).delete_document(event.documentId)
            logger.info("Processed policy archived event - document_id=%s", event.documentId)

        else:
            logger.warning("Ignored RabbitMQ message with unsupported routing_key=%s", routing_key)

    finally:
        db.close()
