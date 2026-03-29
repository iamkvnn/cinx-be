from sqlalchemy.orm import Session
from sqlalchemy import select
from app.entities.course import Course


class CourseRepository:
    def __init__(self, db: Session):
        self.db = db

    def upsert(self, data: dict):
        course = self.db.get(Course, data["id"])
        if course:
            for key, value in data.items():
                setattr(course, key, value)
        else:
            course = Course(**data)
            self.db.add(course)
        self.db.commit()
        return course

    def get_published_courses(self):
        stmt = select(Course).where(Course.is_published == True)
        return self.db.execute(stmt).scalars().all()

    def get_published_courses_by_categories(self, categories: list[str]):
        stmt = select(Course).where(
            Course.is_published == True,
            Course.category.in_(categories)
        )
        return self.db.execute(stmt).scalars().all()

    def get_course_by_id(self, course_id: str):
        return self.db.get(Course, course_id)