from sqlalchemy.orm import Session
from sqlalchemy import select, func
from app.entities.user_interaction import UserInteraction


class InteractionRepository:
    def __init__(self, db: Session):
        self.db = db

    def add_interaction(self, user_id: str, course_id: str, interaction_type: str, weight: float):
        interaction = UserInteraction(
            user_id=user_id,
            course_id=course_id,
            interaction_type=interaction_type,
            weight=weight
        )
        self.db.add(interaction)
        self.db.commit()
        return interaction

    def remove_interaction(self, user_id: str, course_id: str, interaction_type: str):
        stmt = (
            self.db.query(UserInteraction)
            .filter_by(user_id=user_id, course_id=course_id, interaction_type=interaction_type)
            .first()
        )
        if stmt:
            self.db.delete(stmt)
            self.db.commit()

    def count_user_interactions(self, user_id: str) -> int:
        stmt = select(func.count(UserInteraction.id)).where(UserInteraction.user_id == user_id)
        return self.db.execute(stmt).scalar_one()

    def get_user_interacted_course_ids(self, user_id: str) -> list[str]:
        stmt = select(UserInteraction.course_id).where(UserInteraction.user_id == user_id)
        return list(set(self.db.execute(stmt).scalars().all()))