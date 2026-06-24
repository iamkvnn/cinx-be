from dataclasses import dataclass

from sqlalchemy.orm import Session
from sqlalchemy import or_, select, delete
from app.entities.course import Course
from app.entities.course_chunk import CourseChunk
from app.services.embedding_model import embed_documents
import uuid


@dataclass
class CourseFilters:
    query: str | None = None
    category_id: str | None = None
    category_name: str | None = None
    min_rating: float | None = None
    price_from: float | None = None
    price_to: float | None = None
    has_certificate: bool | None = None
    is_in_subscription: bool | None = None
    max_duration: int | None = None

    def has_structured_filters(self) -> bool:
        return any(
            value is not None
            for value in (
                self.category_id,
                self.category_name,
                self.min_rating,
                self.price_from,
                self.price_to,
                self.has_certificate,
                self.is_in_subscription,
                self.max_duration,
            )
        )

class CourseRepository:
    def __init__(self, db: Session):
        self.db = db

    def delete(self, course_id: str):
        course = self.db.get(Course, course_id)
        stmt = delete(CourseChunk).where(CourseChunk.course_id == course_id)
        self.db.execute(stmt)
        if course:
            self.db.delete(course)
        self.db.commit()

    def upsert(self, data: dict):
        course = self.db.get(Course, data["id"])
        
        if course:
            for key, value in data.items():
                setattr(course, key, value)
        else:
            course = Course(**data)
            self.db.add(course)
            
        self.db.commit()

        if course.status == "PUBLISHED":
            self._upsert_course_chunks(
                course.id,
                str(course.title),
                str(course.description or ''),
                course.curriculum or []
            )
            
        return course

    def _upsert_course_chunks(self, course_id: str, course_title: str, course_description: str, curriculum: list):
        # Delete old chunks
        stmt = delete(CourseChunk).where(CourseChunk.course_id == course_id)
        self.db.execute(stmt)

        chunks = [
            CourseChunk(
                id=str(uuid.uuid4()),
                course_id=course_id,
                section_title=None,
                lesson_ids=[],
                content=f"Course: {course_title}. Description: {course_description}.",
                embedding=[],
            )
        ]

        for section in curriculum:
            lessons = section.get("lessons", [])
            lesson_ids = [lesson.get("id") for lesson in lessons if lesson.get("id")]
            lesson_lines = [
                f"Lesson ID: {lesson.get('id', '')}. Title: {lesson.get('title', '')}. Type: {lesson.get('lessonType', '')}."
                for lesson in lessons
            ]
            content = (
                f"Course: {course_title}. Description: {course_description}. "
                f"Section: {section.get('title', '')}. Description: {section.get('description', '')}. "
                + " ".join(lesson_lines)
            )
            chunks.append(
                CourseChunk(
                    id=str(uuid.uuid4()),
                    course_id=course_id,
                    section_title=section.get("title"),
                    lesson_ids=lesson_ids,
                    content=content,
                    embedding=[],
                )
            )

        embeddings = embed_documents([chunk.content for chunk in chunks])
        for chunk, embedding in zip(chunks, embeddings):
            chunk.embedding = embedding
            self.db.add(chunk)

        self.db.commit()

    def get_published_courses(self):
        stmt = select(Course).where(Course.status == "PUBLISHED")
        return self.db.execute(stmt).scalars().all()

    def list_published_categories(self) -> list[dict[str, str | None]]:
        stmt = (
            select(Course.category_id, Course.category_name)
            .where(Course.status == "PUBLISHED", Course.category_name.isnot(None))
            .distinct()
            .order_by(Course.category_name)
        )
        rows = self.db.execute(stmt).all()
        if not isinstance(rows, list):
            return []
        categories = []
        seen = set()
        for category_id, category_name in rows:
            if not category_name:
                continue
            key = (category_id, category_name)
            if key in seen:
                continue
            seen.add(key)
            categories.append({"category_id": category_id, "category_name": category_name})
        return categories

    def get_published_courses_by_categoryId(self, categoryIds: list[str]):
        stmt = select(Course).where(
            Course.status == "PUBLISHED",
            Course.category_id.in_(categoryIds)
        )
        return self.db.execute(stmt).scalars().all()

    def get_by_ids(self, course_ids: list[str]) -> list[Course]:
        if not course_ids:
            return []
        stmt = select(Course).where(Course.id.in_(course_ids), Course.status == "PUBLISHED")
        courses = self.db.execute(stmt).scalars().all()
        course_by_id = {course.id: course for course in courses}
        return [course_by_id[course_id] for course_id in course_ids if course_id in course_by_id]

    def search_published_courses(self, filters: CourseFilters, limit: int = 50) -> list[Course]:
        stmt = select(Course).where(Course.status == "PUBLISHED")
        if filters.query:
            pattern = f"%{filters.query.strip()}%"
            stmt = stmt.where(
                or_(
                    Course.title.ilike(pattern),
                    Course.description.ilike(pattern),
                    Course.category_name.ilike(pattern),
                    Course.instructor_name.ilike(pattern),
                )
            )
        if filters.category_id:
            stmt = stmt.where(Course.category_id == filters.category_id)
        if filters.category_name:
            stmt = stmt.where(Course.category_name.ilike(f"%{filters.category_name.strip()}%"))
        if filters.min_rating is not None:
            stmt = stmt.where(Course.rating >= filters.min_rating)
        if filters.price_from is not None:
            stmt = stmt.where(Course.price >= filters.price_from)
        if filters.price_to is not None:
            stmt = stmt.where(
                or_(
                    Course.discounted_price <= filters.price_to,
                    Course.price <= filters.price_to,
                )
            )
        if filters.has_certificate is not None:
            stmt = stmt.where(Course.has_certificate == filters.has_certificate)
        if filters.is_in_subscription is not None:
            stmt = stmt.where(Course.is_in_subscription == filters.is_in_subscription)
        if filters.max_duration is not None:
            stmt = stmt.where(Course.duration <= filters.max_duration)
        stmt = stmt.order_by(Course.rating.is_(None), Course.rating.desc(), Course.enrollment_count.desc()).limit(limit)
        return self.db.execute(stmt).scalars().all()
