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
        
        curriculum_lines = []
        for section in curriculum:
            curriculum_lines.append(f"Section: {section.get('title', '')}.")
            for lesson in section.get("lessons", []):
                curriculum_lines.append(
                    f"Lesson: {lesson.get('title', '')}. Type: {lesson.get('lessonType', '')}."
                )
        content = f"Course: {course_title}. Description: {course_description}. " + " ".join(curriculum_lines)
                
        chunk = CourseChunk(
            id=str(uuid.uuid4()),
            course_id=course_id,
            content=content,
            embedding=embed_model.encode([content])[0].tolist()
        )
                
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
