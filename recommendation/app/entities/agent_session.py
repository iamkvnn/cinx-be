from datetime import datetime

from sqlalchemy import DateTime, Index, String, Text
from sqlalchemy.dialects.mysql import JSON
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class AgentSession(Base):
    __tablename__ = "agent_sessions"

    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    user_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    title: Mapped[str | None] = mapped_column(String(255), nullable=True)
    state: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


class AgentMessage(Base):
    __tablename__ = "agent_messages"

    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    session_id: Mapped[str] = mapped_column(String(50), nullable=False)
    role: Mapped[str] = mapped_column(String(30), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    intent: Mapped[str | None] = mapped_column(String(50), nullable=True)
    tool_calls: Mapped[list | None] = mapped_column(JSON, nullable=True)
    citations: Mapped[list | None] = mapped_column(JSON, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    __table_args__ = (
        Index("idx_agent_message_session", "session_id"),
        Index("idx_agent_message_created", "created_at"),
    )


class AgentRun(Base):
    __tablename__ = "agent_runs"

    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    session_id: Mapped[str] = mapped_column(String(50), nullable=False)
    user_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    intent: Mapped[str | None] = mapped_column(String(50), nullable=True)
    status: Mapped[str] = mapped_column(String(50), nullable=False, default="STARTED")
    trace: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    error: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    __table_args__ = (
        Index("idx_agent_run_session", "session_id"),
        Index("idx_agent_run_status", "status"),
    )
