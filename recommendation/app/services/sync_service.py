from sqlalchemy import create_engine, text
from app.core.config import settings
from app.repositories.course_repository import CourseRepository
from app.repositories.user_repository import UserRepository
from app.repositories.interaction_repository import InteractionRepository


class SyncService:
    def __init__(self, db):
        self.course_repo = CourseRepository(db)
        self.user_repo = UserRepository(db)
        self.interaction_repo = InteractionRepository(db)

    def sync_courses_from_course_db(self):
        engine_course = create_engine(
            f"mysql+pymysql://{settings.COURSE_DB_USER}:{settings.COURSE_DB_PASSWORD}"
            f"@{settings.COURSE_DB_HOST}:{settings.COURSE_DB_PORT}/{settings.COURSE_DB_NAME}?charset=utf8mb4"
        )
        
        with engine_course.connect() as conn:
            # Get courses
            courses = conn.execute(text("""
                SELECT c.id, c.title, c.description, c.price, c.rating, c.enrollment_count, 
                       c.instructor_id, c.status, c.created_at, c.updated_at,
                       cat.id as category_id, cat.name as category_name
                FROM course c
                LEFT JOIN category cat on c.category_id = cat.id
                WHERE c.status = 2
            """)).mappings().all()

        for row in courses:
            data = {
                "id": row["id"],
                "title": row["title"],
                "description": row["description"],
                "category_id": row["category_id"],
                "category_name": row["category_name"],
                "instructor_id": row["instructor_id"],
                "price": row["price"],
                "rating": row["rating"] if row["rating"] is not None else 0.0,
                "enrollment_count": row["enrollment_count"] if row["enrollment_count"] is not None else 0,
                "status": row["status"] if row["status"] else "DRAFT",
                "created_at": row["created_at"],
                "updated_at": row["updated_at"],
            }
            try:
                self.course_repo.upsert(data)
            except Exception as e:
                print(f"Failed to upsert course {row['id']}: {e}")

    def handle_course_upsert(self, payload):
        status = payload.status if hasattr(payload, "status") else "DRAFT"
        
        # If not PUBLISHED, remove from recommendation DB
        if status != 2:
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
            "price": payload.price,
            "rating": payload.rating if payload.rating is not None else 0.0,
            "enrollment_count": payload.enrollmentCount if payload.enrollmentCount is not None else 0,
            "status": status,
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