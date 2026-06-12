from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    APP_NAME: str = "recommendation"
    APP_ENV: str = "dev"

    DB_HOST: str
    DB_PORT: int
    DB_NAME: str
    DB_USER: str
    DB_PASSWORD: str

    RABBITMQ_HOST: str
    RABBITMQ_PORT: int
    RABBITMQ_USER: str
    RABBITMQ_PASSWORD: str
    RABBITMQ_EXCHANGE: str = "course.events.exchange"
    RABBITMQ_EXCHANGE_TYPE: str = "topic"
    RABBITMQ_QUEUE: str = "recommendation.course.queue"
    RABBITMQ_DLX: str = "dlx.exchange"
    RABBITMQ_DLQ: str = "recommendation.course.dead.queue"
    RABBITMQ_ROUTING_KEYS: str = (
        "course.course.published,course.course.updated,course.course.archived"
    )

    ENROLLMENT_EXCHANGE: str = "enrollment.events.exchange"
    SOCIAL_EXCHANGE: str = "social.events.exchange"
    USER_EXCHANGE: str = "user.events.exchange"
    ENROLLMENT_QUEUE: str = "recommendation.enrollment.queue"
    SOCIAL_QUEUE: str = "recommendation.social.queue"
    USER_QUEUE: str = "recommendation.user.queue"
    ENROLLMENT_ROUTING_KEYS: str = "enrollment.enrollment.created"
    SOCIAL_ROUTING_KEYS: str = "social.wishlist.added,social.wishlist.removed"
    USER_ROUTING_KEYS: str = "user.preference.updated"

    RECONCILE_BATCH_SIZE: int = 100
    INTERACTION_THRESHOLD_FOR_CONTENT_BASED: int = 3

    DIGITALOCEAN_MODEL_ACCESS_KEY: str
    DIGITALOCEAN_INFERENCE_BASE_URL: str
    DIGITALOCEAN_LLM_MODEL: str
    DIGITALOCEAN_LLM_TIMEOUT_SECONDS: float
    DIGITALOCEAN_LLM_MAX_COMPLETION_TOKENS: int = 12000

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")


settings = Settings()
