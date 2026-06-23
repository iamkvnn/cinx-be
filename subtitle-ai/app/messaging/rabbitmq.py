import json
import logging
import asyncio

import aio_pika
import aiormq

from app.core.config import settings
from app.core.logging import reset_context, set_context, trace_headers_from_values
from app.services.processor import SubtitleJobProcessor


logger = logging.getLogger(__name__)

PUBLISH_RETRY_ATTEMPTS = 3
PUBLISH_RETRY_DELAY_SECONDS = 5
AMQP_ERRORS = (
    aiormq.exceptions.AMQPError,
    aio_pika.exceptions.AMQPException,
    ConnectionError,
    OSError,
)


class SubtitleEventPublisher:
    def __init__(self, exchange: aio_pika.Exchange):
        self.exchange = exchange

    async def publish_progress(self, job_id: str, progress_percent: int) -> None:
        logger.info("Publishing subtitle progress job_id=%s progress=%s", job_id, progress_percent)
        await self._publish(
            "ai.subtitle.job.progress",
            {"jobId": job_id, "progressPercent": max(0, min(99, int(progress_percent)))},
        )

    async def publish_completed(self, payload: dict) -> None:
        logger.info("Publishing subtitle completed job_id=%s output_key=%s", payload.get("jobId"), payload.get("outputFileKey"))
        await self._publish("ai.subtitle.job.completed", payload)

    async def publish_failed(self, job_id: str, error_code: str, error_message: str) -> None:
        logger.info("Publishing subtitle failed job_id=%s error_code=%s", job_id, error_code)
        await self._publish(
            "ai.subtitle.job.failed",
            {
                "jobId": job_id,
                "errorCode": error_code,
                "errorMessage": error_message[:4000],
            },
        )

    async def _publish(self, routing_key: str, payload: dict) -> None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        exchange_name = getattr(self.exchange, "name", settings.AI_SUBTITLE_EXCHANGE)
        for attempt in range(1, PUBLISH_RETRY_ATTEMPTS + 1):
            try:
                logger.info("Publishing subtitle event exchange=%s routing_key=%s", exchange_name, routing_key)
                await self.exchange.publish(
                    aio_pika.Message(body=body, content_type="application/json", delivery_mode=aio_pika.DeliveryMode.PERSISTENT),
                    routing_key=routing_key,
                    mandatory=True,
                )
                return
            except AMQP_ERRORS:
                if attempt == PUBLISH_RETRY_ATTEMPTS:
                    logger.exception(
                        "Failed to publish subtitle event exchange=%s routing_key=%s after retries",
                        exchange_name,
                        routing_key,
                    )
                    raise
                logger.warning(
                    "RabbitMQ publish failed exchange=%s routing_key=%s attempt=%s/%s; retrying",
                    exchange_name,
                    routing_key,
                    attempt,
                    PUBLISH_RETRY_ATTEMPTS,
                    exc_info=True,
                )
                await asyncio.sleep(PUBLISH_RETRY_DELAY_SECONDS)


async def start_consumer() -> None:
    logger.info("Starting subtitle AI RabbitMQ consumer - host=%s port=%s", settings.RABBITMQ_HOST, settings.RABBITMQ_PORT)
    connection = await aio_pika.connect_robust(
        host=settings.RABBITMQ_HOST,
        port=settings.RABBITMQ_PORT,
        login=settings.RABBITMQ_USER,
        password=settings.RABBITMQ_PASSWORD,
    )
    consumer_channel = await connection.channel()
    publisher_channel = await connection.channel()
    await consumer_channel.set_qos(prefetch_count=settings.RABBITMQ_PREFETCH)

    course_exchange = await consumer_channel.declare_exchange(settings.COURSE_EXCHANGE, aio_pika.ExchangeType.TOPIC, durable=True)
    ai_exchange = await publisher_channel.declare_exchange(settings.AI_SUBTITLE_EXCHANGE, aio_pika.ExchangeType.TOPIC, durable=True)
    dlx = await consumer_channel.declare_exchange(settings.RABBITMQ_DLX, aio_pika.ExchangeType.DIRECT, durable=True)

    queue = await consumer_channel.declare_queue(
        settings.SUBTITLE_QUEUE,
        durable=True,
        arguments={
            "x-dead-letter-exchange": settings.RABBITMQ_DLX,
            "x-dead-letter-routing-key": "subtitle-ai.course.dead",
        },
    )
    dlq = await consumer_channel.declare_queue(settings.SUBTITLE_DLQ, durable=True)
    await dlq.bind(dlx, "subtitle-ai.course.dead")

    for routing_key in [key.strip() for key in settings.SUBTITLE_ROUTING_KEYS.split(",") if key.strip()]:
        await queue.bind(course_exchange, routing_key)
        logger.info("Bound subtitle AI queue=%s routing_key=%s", settings.SUBTITLE_QUEUE, routing_key)

    publisher = SubtitleEventPublisher(ai_exchange)
    processor = SubtitleJobProcessor()

    async with queue.iterator() as queue_iter:
        async for message in queue_iter:
            try:
                async with message.process(requeue=False):
                    headers = message.headers or {}
                    trace_headers = trace_headers_from_values(
                        str(headers.get("traceparent")) if headers.get("traceparent") else None,
                        str(headers.get("X-Request-Id")) if headers.get("X-Request-Id") else None,
                    )
                    tokens = set_context(trace_headers)
                    try:
                        payload = json.loads(message.body.decode("utf-8"))
                        logger.info(
                            "Received subtitle AI message routing_key=%s job_id=%s delivery_tag=%s",
                            message.routing_key,
                            payload.get("jobId"),
                            message.delivery_tag,
                        )
                        await dispatch_message(message.routing_key, payload, processor, publisher)
                    except Exception as exc:
                        logger.exception("Failed to process subtitle AI message routing_key=%s", message.routing_key)
                        await publish_failure_from_body(message.body, publisher, exc)
                    finally:
                        reset_context(tokens)
            except AMQP_ERRORS:
                logger.warning("RabbitMQ channel closed while processing subtitle message; waiting for reconnect", exc_info=True)


async def dispatch_message(routing_key: str, payload: dict, processor: SubtitleJobProcessor, publisher: SubtitleEventPublisher) -> None:
    if routing_key == "course.subtitle.generate.requested":
        await processor.process_generate(payload, publisher)
    elif routing_key == "course.subtitle.translate.requested":
        await processor.process_translate(payload, publisher)
    else:
        logger.warning("Ignored unsupported subtitle request routing key: %s", routing_key)


async def publish_failure_from_body(body: bytes, publisher: SubtitleEventPublisher, exc: Exception) -> None:
    try:
        payload = json.loads(body.decode("utf-8"))
        job_id = payload.get("jobId")
    except Exception:
        job_id = None
    if job_id:
        try:
            await publisher.publish_failed(str(job_id), "AI_SUBTITLE_FAILED", str(exc))
        except AMQP_ERRORS:
            logger.exception("Failed to publish subtitle failure event job_id=%s because RabbitMQ is unavailable", job_id)
