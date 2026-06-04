from sqlalchemy import create_engine, inspect, text
from sqlalchemy.orm import sessionmaker, DeclarativeBase
from app.core.config import settings

DATABASE_URL = (
    f"mysql+pymysql://{settings.DB_USER}:{settings.DB_PASSWORD}"
    f"@{settings.DB_HOST}:{settings.DB_PORT}/{settings.DB_NAME}?charset=utf8mb4"
)

engine = create_engine(
    DATABASE_URL,
    pool_pre_ping=True,
    pool_recycle=3600,
    future=True
)

SessionLocal = sessionmaker(
    bind=engine,
    autoflush=False,
    autocommit=False,
    expire_on_commit=False
)


class Base(DeclarativeBase):
    pass


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

def create_tables():
    Base.metadata.create_all(bind=engine)


def ensure_schema():
    Base.metadata.create_all(bind=engine)
    inspector = inspect(engine)
    if "courses" not in inspector.get_table_names():
        return
    course_columns = {column["name"] for column in inspector.get_columns("courses")}
    missing_columns = {
        "title": "ADD COLUMN title VARCHAR(255) NULL",
        "description": "ADD COLUMN description TEXT NULL",
        "category_id": "ADD COLUMN category_id VARCHAR(50) NULL",
        "category_name": "ADD COLUMN category_name VARCHAR(100) NULL",
        "instructor_id": "ADD COLUMN instructor_id VARCHAR(50) NULL",
        "rating": "ADD COLUMN rating FLOAT NOT NULL DEFAULT 0",
        "enrollment_count": "ADD COLUMN enrollment_count INT NOT NULL DEFAULT 0",
        "status": "ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'DRAFT'",
        "publish_status": "ADD COLUMN publish_status VARCHAR(50) NULL",
        "curriculum": "ADD COLUMN curriculum JSON NULL",
        "created_at": "ADD COLUMN created_at DATETIME NULL",
        "updated_at": "ADD COLUMN updated_at DATETIME NULL",
    }
    with engine.begin() as conn:
        for column_name, ddl in missing_columns.items():
            if column_name not in course_columns:
                conn.execute(text(f"ALTER TABLE courses {ddl}"))

    if "user_preferences" not in inspector.get_table_names():
        return
    user_preference_columns = {column["name"] for column in inspector.get_columns("user_preferences")}
    user_preference_missing_columns = {
        "user_id": "ADD COLUMN user_id VARCHAR(50) NULL",
        "categoryId": "ADD COLUMN categoryId VARCHAR(100) NULL",
        "created_at": "ADD COLUMN created_at DATETIME NULL",
    }
    with engine.begin() as conn:
        for column_name, ddl in user_preference_missing_columns.items():
            if column_name not in user_preference_columns:
                conn.execute(text(f"ALTER TABLE user_preferences {ddl}"))
        if "category" in user_preference_columns and "categoryId" not in user_preference_columns:
            conn.execute(text("UPDATE user_preferences SET categoryId = category WHERE categoryId IS NULL"))
