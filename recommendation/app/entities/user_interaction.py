from datetime import datetime
from sqlalchemy import String, DateTime, Float, Index
from sqlalchemy.orm import Mapped, mapped_column
from app.core.database import Base


class UserInteraction(Base):
    __tablename__ = "user_interactions"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    user_id: Mapped[str] = mapped_column(String(64), nullable=False)
    course_id: Mapped[str] = mapped_column(String(64), nullable=False)
    interaction_type: Mapped[str] = mapped_column(String(50), nullable=False)
    weight: Mapped[float] = mapped_column(Float, nullable=False, default=1.0)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    __table_args__ = (
        Index("idx_user_interactions_user", "user_id"),
        Index("idx_user_interactions_course", "course_id"),
        Index("idx_user_interactions_user_course", "user_id", "course_id"),
    )