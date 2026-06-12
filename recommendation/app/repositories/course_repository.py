from sqlalchemy.orm import Session
from sqlalchemy import select, delete
from app.entities.course import Course
from app.entities.course_chunk import CourseChunk
from sentence_transformers import SentenceTransformer
import uuid

embed_model = SentenceTransformer("all-MiniLM-L6-v2")

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

        embeddings = embed_model.encode([chunk.content for chunk in chunks])
        for chunk, embedding in zip(chunks, embeddings):
            chunk.embedding = embedding.tolist()
            self.db.add(chunk)

        self.db.commit()

    def get_published_courses(self):
        stmt = select(Course).where(Course.status == "PUBLISHED")
        return self.db.execute(stmt).scalars().all()

    def get_published_courses_by_categoryId(self, categoryIds: list[str]):
        stmt = select(Course).where(
            Course.status == "PUBLISHED",
            Course.category_id.in_(categoryIds)
        )
        return self.db.execute(stmt).scalars().all()
