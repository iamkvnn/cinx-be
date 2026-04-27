from app.repositories.course_repository import CourseRepository
from app.repositories.user_repository import UserRepository
from app.repositories.interaction_repository import InteractionRepository


class SyncService:
    def __init__(self, db):
        self.course_repo = CourseRepository(db)
        self.user_repo = UserRepository(db)
        self.interaction_repo = InteractionRepository(db)

    def handle_course_upsert(self, payload):
        category = payload.category or {}
        instructor = payload.instructor or {}

        data = {
            "id": payload.id,
            "title": payload.title,
            "description": payload.description,

            # Category (flattened)
            "category_id": category.get("id") if isinstance(category, dict) else getattr(category, "id", None),
            "category_name": category.get("name") if isinstance(category, dict) else getattr(category, "name", None),

            # Instructor (flattened)
            "instructor_id": instructor.get("id") if isinstance(instructor, dict) else getattr(instructor, "id", None),
            "instructor_name": instructor.get("name") if isinstance(instructor, dict) else getattr(instructor, "name", None),
            "instructor_email": instructor.get("email") if isinstance(instructor, dict) else getattr(instructor, "email", None),
            "instructor_gender": instructor.get("gender") if isinstance(instructor, dict) else getattr(instructor, "gender", None),
            "instructor_avatar_url": instructor.get("avatarUrl") if isinstance(instructor, dict) else getattr(instructor, "avatarUrl", None),

            # Images
            "images": [img.model_dump() for img in payload.images] if hasattr(payload, "images") and payload.images else None,

            "price": payload.price,
            "discounted_price": payload.discountedPrice,
            "discount_rate": payload.discountRate,
            "rating": payload.rating if payload.rating is not None else 0.0,
            "enrollment_count": payload.enrollmentCount if payload.enrollmentCount is not None else 0,
            "is_in_subscription": payload.isInSubscription,
            "status": payload.status,  # "DRAFT" | "PUBLISHED" | ...
            "duration": payload.duration,
            "has_certificate": payload.hasCertificate if payload.hasCertificate is not None else False,
            "certificate_title": payload.certificateTitle,
            "sections": [s.model_dump() for s in payload.sections] if hasattr(payload, "sections") and payload.sections else None,
            "created_at": payload.createdAt,
            "updated_at": payload.updatedAt,
        }
        self.course_repo.upsert(data)

    def handle_enrollment(self, user_id: str, course_id: str):
        self.interaction_repo.add_interaction(user_id, course_id, "ENROLL", 5.0)

    def handle_wishlist(self, user_id: str, course_id: str, added: bool):
        if added:
            self.interaction_repo.add_interaction(user_id, course_id, "WISHLIST", 2.0)
        else:
            self.interaction_repo.remove_interaction(user_id, course_id, "WISHLIST")

    def handle_user_preferences(self, payload):
        self.user_repo.replace_user_preferences(payload.userId, payload.categories)

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