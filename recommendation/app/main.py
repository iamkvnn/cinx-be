import asyncio
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.core.config import settings
from app.core.database import Base, engine, SessionLocal
from app.api.recommendation import router as recommendation_router
from app.api.learning_path import router as learning_path_router
from app.messaging.rabbitmq_consumer import start_consumer
from app.services.sync_service import SyncService


logger = logging.getLogger(__name__)

async def scheduled_course_sync():
    while True:
        try:
            logger.info("Starting scheduled sync from course db...")
            db = SessionLocal()
            try:
                sync_service = SyncService(db)
                # Run sync in thread pool to prevent blocking the async event loop
                await asyncio.to_thread(sync_service.sync_courses_from_course_db)
                logger.info("Successfully synced courses from course db.")
            finally:
                db.close()
        except asyncio.CancelledError:
            break
        except Exception as e:
            logger.error(f"Error syncing courses: {e}")
        
        # Wait 12 hours before next sync
        await asyncio.sleep(12 * 3600)

@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)

    consumer_task = asyncio.create_task(start_consumer())
    sync_task = asyncio.create_task(scheduled_course_sync())
    yield
    consumer_task.cancel()
    sync_task.cancel()


app = FastAPI(title=settings.APP_NAME, lifespan=lifespan)
app.include_router(recommendation_router)
app.include_router(learning_path_router)