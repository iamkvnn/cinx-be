from sqlalchemy import String, Text, Integer
from sqlalchemy.orm import Mapped, mapped_column
from sqlalchemy.dialects.mysql import JSON
from app.core.database import Base

class CourseChunk(Base):
    __tablename__ = "course_chunks"

    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    course_id: Mapped[str] = mapped_column(String(50), nullable=False)
    section_id: Mapped[str | None] = mapped_column(String(50), nullable=True)
    lesson_id: Mapped[str | None] = mapped_column(String(50), nullable=True)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    embedding: Mapped[list[float]] = mapped_column(JSON, nullable=False)