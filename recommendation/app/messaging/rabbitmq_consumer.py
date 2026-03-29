import aio_pika
import asyncio
from app.core.config import settings
from app.messaging.event_handlers import process_message

async def start_consumer():
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
    routing_keys_course = [rk.strip() for rk in settings.RABBITMQ_ROUTING_KEYS.split(",")]
    for routing_key in routing_keys_course:
        await queue_course.bind(exchange_course, routing_key)

    # Setup Enrollment queue
    exchange_enrollment = await channel.declare_exchange(
        settings.ENROLLMENT_EXCHANGE,
        aio_pika.ExchangeType.TOPIC,
        durable=True
    )
    queue_enrollment = await channel.declare_queue(settings.ENROLLMENT_QUEUE, durable=True)
    routing_keys_enrollment = [rk.strip() for rk in settings.ENROLLMENT_ROUTING_KEYS.split(",")]
    for routing_key in routing_keys_enrollment:
        await queue_enrollment.bind(exchange_enrollment, routing_key)

    # Setup Social queue
    exchange_social = await channel.declare_exchange(
        settings.SOCIAL_EXCHANGE,
        aio_pika.ExchangeType.TOPIC,
        durable=True
    )
    queue_social = await channel.declare_queue(settings.SOCIAL_QUEUE, durable=True)
    routing_keys_social = [rk.strip() for rk in settings.SOCIAL_ROUTING_KEYS.split(",")]
    for routing_key in routing_keys_social:
        await queue_social.bind(exchange_social, routing_key)


    async def consume_queue(queue):
        async with queue.iterator() as queue_iter:
            async for message in queue_iter:
                async with message.process():
                    await process_message(message.body, message.routing_key)
                    
    # Run all queues concurrently
    await asyncio.gather(
        consume_queue(queue_course),
        consume_queue(queue_enrollment),
        consume_queue(queue_social)
    )