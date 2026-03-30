from sqlalchemy import String, Text, Boolean, Integer, Numeric, Float, DateTime, Index
from sqlalchemy.orm import Mapped, mapped_column
from sqlalchemy.dialects.mysql import JSON
from datetime import datetime
from app.core.database import Base


class Course(Base):
    __tablename__ = "courses"

    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    description: Mapped[str | None] = mapped_column(Text, nullable=True)
    category: Mapped[str] = mapped_column(String(100), nullable=False)

    price: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)
    discounted_price: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)
    discount_rate: Mapped[int | None] = mapped_column(Integer, nullable=True)

    rating: Mapped[float] = mapped_column(Float, nullable=False, default=0.0)
    enrollment_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0)

    is_published: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    is_in_subscription: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)

    duration: Mapped[int | None] = mapped_column(Integer, nullable=True)    
    sections: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    created_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    updated_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        Index("idx_course_category", "category"),
        Index("idx_course_published", "is_published"),
        Index("idx_course_rating", "rating"),
        Index("idx_course_enrollment", "enrollment_count"),
    )