from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    APP_NAME: str = "recommendation"
    APP_ENV: str = "dev"

    DB_HOST: str = "localhost"
    DB_PORT: int = 3306
    DB_NAME: str = "recommendationdb"
    DB_USER: str = "root"
    DB_PASSWORD: str = "taolao123"

    RABBITMQ_HOST: str = "rabbitmq"
    RABBITMQ_PORT: int = 5672
    RABBITMQ_USER: str = "guest"
    RABBITMQ_PASSWORD: str = "guest"
    RABBITMQ_EXCHANGE: str = "recommendation.events.exchange"
    RABBITMQ_EXCHANGE_TYPE: str = "topic"
    RABBITMQ_QUEUE: str = "recommendation.course.queue"
    RABBITMQ_DLX: str = "dlx.exchange"
    RABBITMQ_DLQ: str = "recommendation.course.dead.queue"
    RABBITMQ_ROUTING_KEYS: str = (
        "course.course.created,course.course.updated,course.course.published,"
        "course.course.unpublished,course.course.deleted,"
        "user.preference.selected,user.preference.updated,"
        "course.course.viewed,course.course.wishlisted,course.course.enrolled,course.course.completed,course.course.rated"
    )

    # Source services (for reconciliation)
    COURSE_SERVICE_BASE_URL: str = "http://localhost:9090/api/v1/courses"
    USER_SERVICE_BASE_URL: str = "http://localhost:8080/users"
    ENROLLMENT_SERVICE_BASE_URL: str = "http://localhost:8080/enrollments"

    RECONCILE_BATCH_SIZE: int = 100
    INTERACTION_THRESHOLD_FOR_CONTENT_BASED: int = 3

    model_config = SettingsConfigDict(env_file=".env", case_sensitive=True)


settings = Settings()