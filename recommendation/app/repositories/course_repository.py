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

    def upsert(self, data: dict):
        course = self.db.get(Course, data["id"])
        
        # Save course chunks
        sections = data.pop("sections", None)
        
        if course:
            for key, value in data.items():
                setattr(course, key, value)
            if sections is not None:
                course.sections = sections
        else:
            if sections is not None:
                data["sections"] = sections
            course = Course(**data)
            self.db.add(course)
            
        self.db.commit()

        if sections is not None and course.status == "PUBLISHED":
            self._upsert_course_chunks(course.id, str(course.title), str(course.description or ''), sections)
            
        return course

    def _upsert_course_chunks(self, course_id: str, course_title: str, course_description: str, sections: list):
        # Delete old chunks
        stmt = delete(CourseChunk).where(CourseChunk.course_id == course_id)
        self.db.execute(stmt)
        
        # Build new chunks
        chunks_to_insert = []
        for sec in sections:
            sec_title = sec.get("title", "")
            for les in sec.get("lessons", []):
                les_id = les.get("id")
                les_title = les.get("title", "")
                les_type = les.get("lessonType", "")
                les_desc = les.get("description", "")
                
                content = f"Course: {course_title}. Description: {course_description}. Section: {sec_title}. Lesson: {les_title} ({les_type}). {les_desc}"
                
                chunk = CourseChunk(
                    id=str(uuid.uuid4()),
                    course_id=course_id,
                    section_id=sec.get("id"),
                    lesson_id=les_id,
                    content=content,
                    embedding=embed_model.encode([content])[0].tolist()
                )
                chunks_to_insert.append(chunk)
                
        if chunks_to_insert:
            self.db.add_all(chunks_to_insert)
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