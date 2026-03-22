import aio_pika
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

    exchange = await channel.declare_exchange(
        settings.RABBITMQ_EXCHANGE,
        aio_pika.ExchangeType.TOPIC,
        durable=True
    )

    queue = await channel.declare_queue(settings.RABBITMQ_QUEUE, durable=True)

    routing_keys = [rk.strip() for rk in settings.RABBITMQ_ROUTING_KEYS.split(",")]
    for routing_key in routing_keys:
        await queue.bind(exchange, routing_key)

    async with queue.iterator() as queue_iter:
        async for message in queue_iter:
            async with message.process():
                await process_message(message.body)