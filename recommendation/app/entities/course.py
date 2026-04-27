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

    # Category (stored flat)
    category_id: Mapped[str | None] = mapped_column(String(50), nullable=True)
    category_name: Mapped[str | None] = mapped_column(String(100), nullable=True)

    # Instructor (stored flat)
    instructor_id: Mapped[str | None] = mapped_column(String(50), nullable=True)
    instructor_name: Mapped[str | None] = mapped_column(String(255), nullable=True)
    instructor_email: Mapped[str | None] = mapped_column(String(255), nullable=True)
    instructor_gender: Mapped[str | None] = mapped_column(String(10), nullable=True)
    instructor_avatar_url: Mapped[str | None] = mapped_column(Text, nullable=True)

    # Images (JSON array)
    images: Mapped[dict | None] = mapped_column(JSON, nullable=True)

    price: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)
    discounted_price: Mapped[float | None] = mapped_column(Numeric(12, 2), nullable=True)
    discount_rate: Mapped[int | None] = mapped_column(Integer, nullable=True)

    rating: Mapped[float] = mapped_column(Float, nullable=False, default=0.0)
    enrollment_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0)

    is_in_subscription: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    status: Mapped[str] = mapped_column(String(20), nullable=False, default="DRAFT")  # DRAFT | PUBLISHED | ...

    duration: Mapped[int | None] = mapped_column(Integer, nullable=True)

    has_certificate: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    certificate_title: Mapped[str | None] = mapped_column(String(255), nullable=True)

    sections: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    created_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    updated_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        Index("idx_course_category_id", "category_id"),
        Index("idx_course_status", "status"),
        Index("idx_course_rating", "rating"),
        Index("idx_course_enrollment", "enrollment_count"),
    )