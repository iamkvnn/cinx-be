from app.repositories.course_repository import CourseRepository
from app.repositories.user_repository import UserRepository
from app.repositories.interaction_repository import InteractionRepository


class SyncService:
    def __init__(self, db):
        self.course_repo = CourseRepository(db)
        self.user_repo = UserRepository(db)
        self.interaction_repo = InteractionRepository(db)

    def handle_course_upsert(self, payload):
        status_raw = payload.status if hasattr(payload, "status") else "DRAFT"
        status = str(status_raw).upper() if isinstance(status_raw, str) else ("PUBLISHED" if status_raw == 2 else "DRAFT")
        
        is_published = getattr(payload, "isPublished", False)
        if not is_published or status == "ARCHIVED":
            self.course_repo.delete(payload.id)
            return

        category = payload.category or {}
        instructor = payload.instructor or {}

        data = {
            "id": payload.id,
            "title": payload.title,
            "description": payload.description,
            "category_id": category.get("id") if isinstance(category, dict) else getattr(category, "id", None),
            "category_name": category.get("name") if isinstance(category, dict) else getattr(category, "name", None),
            "instructor_id": instructor.get("id") if isinstance(instructor, dict) else getattr(instructor, "id", None),
            "is_published": is_published,
            "rating": payload.rating if payload.rating is not None else 0.0,
            "enrollment_count": payload.enrollmentCount if payload.enrollmentCount is not None else 0,
            "status": status,
            "created_at": payload.createdAt,
            "updated_at": payload.updatedAt,
        }
        if payload.sections is not None:
            data["curriculum"] = [section.model_dump(mode="json") for section in payload.sections]
        self.course_repo.upsert(data)

    def handle_enrollment(self, user_id: str, course_id: str):
        self.interaction_repo.add_interaction(user_id, course_id, "ENROLL", 5.0)

    def handle_wishlist(self, user_id: str, course_id: str, added: bool):
        if added:
            self.interaction_repo.add_interaction(user_id, course_id, "WISHLIST", 2.0)
        else:
            self.interaction_repo.remove_interaction(user_id, course_id, "WISHLIST")

    def handle_user_preferences(self, payload):
        self.user_repo.replace_user_preferences(payload.userId, payload.categoryIds)

    def handle_interaction(self, payload):
        weights = {
            "VIEW": 1.0,
            "WISHLIST": 2.0,
            "ENROLL": 5.0,
            "COMPLETE": 7.0,
            "RATING": 3.0,
        }
        weight = weights.get(payload.interactionType.upper(), 1.0)
        self.interaction_repo.add_interaction(
            user_id=payload.userId,
            course_id=payload.courseId,
            interaction_type=payload.interactionType.upper(),
            weight=weight
        )
