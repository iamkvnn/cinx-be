import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.core.config import settings
from app.core.database import ensure_schema
from app.api.recommendation import router as recommendation_router
from app.api.learning_path import router as learning_path_router
from app.messaging.rabbitmq_consumer import start_consumer


logger = logging.getLogger(__name__)

def configure_logging():
    logging.getLogger("app").setLevel(logging.INFO)
    logging.getLogger(__name__).setLevel(logging.INFO)
    root_logger = logging.getLogger()
    root_logger.setLevel(logging.INFO)
    if not root_logger.handlers:
        logging.basicConfig(
            level=logging.INFO,
            format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
        )

def log_task_result(task):
    if task.cancelled():
        return
    try:
        task.result()
    except Exception:
        logger.exception("Recommendation RabbitMQ consumer stopped unexpectedly")

@asynccontextmanager
async def lifespan(app: FastAPI):
    configure_logging()
    logger.info("Starting recommendation service")
    ensure_schema()

    import asyncio
    consumer_task = asyncio.create_task(start_consumer())
    consumer_task.add_done_callback(log_task_result)
    yield
    consumer_task.cancel()


app = FastAPI(title=settings.APP_NAME, lifespan=lifespan)
app.include_router(recommendation_router)
app.include_router(learning_path_router)
