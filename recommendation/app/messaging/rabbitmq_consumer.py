import aio_pika
import asyncio
import logging
from app.core.config import settings
from app.messaging.event_handlers import process_message

logger = logging.getLogger(__name__)

async def start_consumer():
    logger.info("Starting RabbitMQ consumer - host=%s port=%s", settings.RABBITMQ_HOST, settings.RABBITMQ_PORT)
    connection = await aio_pika.connect_robust(
        host=settings.RABBITMQ_HOST,
        port=settings.RABBITMQ_PORT,
        login=settings.RABBITMQ_USER,
        password=settings.RABBITMQ_PASSWORD,
    )

    channel = await connection.channel()
    await channel.set_qos(prefetch_count=50)

    # Setup Course queue
    exchange_course = await channel.declare_exchange(
        settings.RABBITMQ_EXCHANGE,
        aio_pika.ExchangeType.TOPIC,
        durable=True
    )
    queue_course = await channel.declare_queue(settings.RABBITMQ_QUEUE, durable=True)
    queue_course_error = await channel.declare_queue(f"{settings.RABBITMQ_QUEUE}.error", durable=True)
    routing_keys_course = [rk.strip() for rk in settings.RABBITMQ_ROUTING_KEYS.split(",")]
    for routing_key in routing_keys_course:
        await queue_course.bind(exchange_course, routing_key)
        logger.info("Bound recommendation course queue=%s routing_key=%s", settings.RABBITMQ_QUEUE, routing_key)

    # Setup Enrollment queue
    exchange_enrollment = await channel.declare_exchange(
        settings.ENROLLMENT_EXCHANGE,
        aio_pika.ExchangeType.TOPIC,
        durable=True
    )
    queue_enrollment = await channel.declare_queue(settings.ENROLLMENT_QUEUE, durable=True)
    queue_enrollment_error = await channel.declare_queue(f"{settings.ENROLLMENT_QUEUE}.error", durable=True)
    routing_keys_enrollment = [rk.strip() for rk in settings.ENROLLMENT_ROUTING_KEYS.split(",")]
    for routing_key in routing_keys_enrollment:
        await queue_enrollment.bind(exchange_enrollment, routing_key)
        logger.info("Bound recommendation enrollment queue=%s routing_key=%s", settings.ENROLLMENT_QUEUE, routing_key)

    # Setup Social queue
    exchange_social = await channel.declare_exchange(
        settings.SOCIAL_EXCHANGE,
        aio_pika.ExchangeType.TOPIC,
        durable=True
    )
    queue_social = await channel.declare_queue(settings.SOCIAL_QUEUE, durable=True)
    queue_social_error = await channel.declare_queue(f"{settings.SOCIAL_QUEUE}.error", durable=True)
    routing_keys_social = [rk.strip() for rk in settings.SOCIAL_ROUTING_KEYS.split(",")]
    for routing_key in routing_keys_social:
        await queue_social.bind(exchange_social, routing_key)
        logger.info("Bound recommendation social queue=%s routing_key=%s", settings.SOCIAL_QUEUE, routing_key)

    # Setup User queue
    exchange_user = await channel.declare_exchange(
        settings.USER_EXCHANGE,
        aio_pika.ExchangeType.TOPIC,
        durable=True
    )
    queue_user = await channel.declare_queue(settings.USER_QUEUE, durable=True)
    queue_user_error = await channel.declare_queue(f"{settings.USER_QUEUE}.error", durable=True)
    routing_keys_user = [rk.strip() for rk in settings.USER_ROUTING_KEYS.split(",")]
    for routing_key in routing_keys_user:
        await queue_user.bind(exchange_user, routing_key)
        logger.info("Bound recommendation user queue=%s routing_key=%s", settings.USER_QUEUE, routing_key)


    async def consume_queue(queue, error_queue):
        async with queue.iterator() as queue_iter:
            async for message in queue_iter:
                async with message.process():
                    try:
                        await process_message(message.body, message.routing_key)
                    except Exception:
                        logger.exception(
                            "Failed to process RabbitMQ message - queue=%s routing_key=%s message_id=%s",
                            queue.name,
                            message.routing_key,
                            message.message_id,
                        )
                        # await channel.default_exchange.publish(
                        #     aio_pika.Message(
                        #         body=message.body,
                        #         content_type=message.content_type,
                        #         headers={
                        #             **(message.headers or {}),
                        #             "failedRoutingKey": message.routing_key,
                        #             "failedQueue": queue.name,
                        #         },
                        #     ),
                        #     routing_key=error_queue.name,
                        # )
                    
    # Run all queues concurrently
    await asyncio.gather(
        consume_queue(queue_course, queue_course_error),
        consume_queue(queue_enrollment, queue_enrollment_error),
        consume_queue(queue_social, queue_social_error),
        consume_queue(queue_user, queue_user_error)
    )
