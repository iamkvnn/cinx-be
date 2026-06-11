from contextlib import asynccontextmanager
import asyncio
import logging

from fastapi import FastAPI

from app.core.config import settings
from app.core.logging import configure_logging
from app.messaging.rabbitmq import start_consumer


logger = logging.getLogger(__name__)


def log_task_result(task: asyncio.Task) -> None:
    if task.cancelled():
        return
    try:
        task.result()
    except Exception:
        logger.exception("Subtitle AI RabbitMQ consumer stopped unexpectedly")


@asynccontextmanager
async def lifespan(app: FastAPI):
    configure_logging()
    logger.info("Starting subtitle AI service")
    consumer_task = asyncio.create_task(start_consumer())
    consumer_task.add_done_callback(log_task_result)
    yield
    consumer_task.cancel()


app = FastAPI(title=settings.APP_NAME, lifespan=lifespan)


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "UP"}
