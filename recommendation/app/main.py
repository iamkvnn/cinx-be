import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.core.config import settings
from app.core.database import Base, engine
from app.api.recommendation import router as recommendation_router
from app.messaging.rabbitmq_consumer import start_consumer


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)

    consumer_task = asyncio.create_task(start_consumer())
    yield
    consumer_task.cancel()


app = FastAPI(title=settings.APP_NAME, lifespan=lifespan)
app.include_router(recommendation_router)