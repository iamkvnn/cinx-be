from app.repositories.course_repository import CourseRepository
from app.repositories.user_repository import UserRepository
from app.repositories.interaction_repository import InteractionRepository


class SyncService:
    def __init__(self, db):
        self.course_repo = CourseRepository(db)
        self.user_repo = UserRepository(db)
        self.interaction_repo = InteractionRepository(db)

    def handle_course_upsert(self, payload):
        data = {
            "id": payload.id,
            "title": payload.title,
            "description": payload.description,
            "category": payload.category,
            "price": payload.price,
            "discounted_price": payload.discountedPrice,
            "discount_rate": payload.discountRate,
            "rating": payload.rating,
            "enrollment_count": payload.enrollmentCount,
            "is_published": payload.isPublished,
            "is_in_subscription": payload.isInSubscription,
            "duration": payload.duration,
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