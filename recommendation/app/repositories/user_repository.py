from sqlalchemy.orm import Session
from sqlalchemy import select, delete
from app.entities.user_preference import UserPreference


class UserRepository:
    def __init__(self, db: Session):
        self.db = db

    def replace_user_preferences(self, user_id: str, categories: list[str]):
        self.db.execute(delete(UserPreference).where(UserPreference.user_id == user_id))
        for category in categories:
            self.db.add(UserPreference(user_id=user_id, categoryId=category))
        self.db.commit()

    def get_user_categories(self, user_id: str) -> list[str]:
        stmt = select(UserPreference.categoryId).where(UserPreference.user_id == user_id)
        return list(self.db.execute(stmt).scalars().all())
