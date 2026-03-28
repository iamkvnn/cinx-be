from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    APP_NAME: str = "recommendation"
    APP_ENV: str = "dev"

    DB_HOST: str = "localhost"
    DB_PORT: int = 3306
    DB_NAME: str = "recommendationdb"
    DB_USER: str = "root"
    DB_PASSWORD: str = "taolao123"

    RABBITMQ_HOST: str = "localhost"
    RABBITMQ_PORT: int = 5672
    RABBITMQ_USER: str = "guest"
    RABBITMQ_PASSWORD: str = "guest"
    RABBITMQ_EXCHANGE: str = "course.events.exchange"
    RABBITMQ_EXCHANGE_TYPE: str = "topic"
    RABBITMQ_QUEUE: str = "recommendation.course.queue"
    RABBITMQ_DLX: str = "dlx.exchange"
    RABBITMQ_DLQ: str = "recommendation.course.dead.queue"
    RABBITMQ_ROUTING_KEYS: str = (
        "course.course.created,course.course.updated,course.course.published"
    )

    ENROLLMENT_EXCHANGE: str = "enrollment.events.exchange"
    SOCIAL_EXCHANGE: str = "social.events.exchange"
    ENROLLMENT_QUEUE: str = "recommendation.enrollment.queue"
    SOCIAL_QUEUE: str = "recommendation.social.queue"
    ENROLLMENT_ROUTING_KEYS: str = "enrollment.enrollment.created"
    SOCIAL_ROUTING_KEYS: str = "social.wishlist.added,social.wishlist.removed"

    # Source services (for reconciliation)
    COURSE_SERVICE_BASE_URL: str = "http://localhost:9090/api/v1/courses"
    USER_SERVICE_BASE_URL: str = "http://localhost:8080/users"
    ENROLLMENT_SERVICE_BASE_URL: str = "http://localhost:8080/enrollments"

    RECONCILE_BATCH_SIZE: int = 100
    INTERACTION_THRESHOLD_FOR_CONTENT_BASED: int = 3

    model_config = SettingsConfigDict(env_file=".env", case_sensitive=True)


settings = Settings()