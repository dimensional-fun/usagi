import com.rabbitmq.client.AMQP
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import dimensional.usagi.Usagi
import dimensional.usagi.channel.consumer.on
import dimensional.usagi.channel.event.MessagePublishedEvent
import dimensional.usagi.channel.method.basic
import dimensional.usagi.channel.method.exchange
import dimensional.usagi.channel.method.queue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import mixtape.oss.kyuso.Kyuso
import mixtape.oss.kyuso.tools.calculatingDelay
import kotlin.time.Duration.Companion.seconds

val publishData = "lol".encodeToByteArray()
val publishHeaders = mapOf("X-Testing" to true)

suspend fun main() {
    usagi()
}

suspend fun javaRabbitMq() {
    val connectionFactory = ConnectionFactory()
    connectionFactory.useNio()
    connectionFactory.setUri("amqp://localhost")

    /* create connection & channel */
    val connection = connectionFactory.newConnection()
    val channel = connection.createChannel()

    /* declare exchange */
    channel.exchangeDeclare("test", "direct", false, true, emptyMap())

    /* declare & bind queue */
    channel.queueDeclare("test", true, false, true, emptyMap())
    channel.queueBind("", "test", "test")

    /* consume messages */
    channel.basicConsume(
        "",
        DeliverCallback { _, message ->
            println(message.body.decodeToString());
            println(message.properties);
            println(message.envelope)
        },
        CancelCallback {  }
    )

    /* publish messages every 20ms */
    val kyuso = Kyuso(Dispatchers.IO.limitedParallelism(1))
    kyuso.dispatchEvery(calculatingDelay(1.seconds)) {
        val properties = AMQP.BasicProperties.Builder()
            .headers(publishHeaders)
            .build()

        channel.basicPublish("test", "test", properties, publishData)
    }

    kyuso.scope.coroutineContext.job.join()
}

suspend fun usagi() {
    val connection = Usagi("amqp://localhost") {
        properties {
            connectionName = "testing thing idk"
        }
    }

    val channel = connection.channels.create()!!

    channel.queue.declare {
        queue = "test"
        durable = true
        autoDelete = true
    }

    channel.exchange.declare {
        exchange = "test"
        autoDelete = true
    }

    channel.queue.bind {
        exchange = "test"
        routingKey = "test"
    }

    val consumer = channel.basic.consume {
        queue = "test"
    }

    consumer.on<MessagePublishedEvent> {
        println(delivery.data.decodeToString())
        println(delivery.properties)
        println(delivery.envelope)

        delivery.ack()
    }

    val kyuso = Kyuso(Dispatchers.IO.limitedParallelism(1))
    kyuso.dispatchEvery(calculatingDelay(1.seconds)) {
        channel.basic.publish {
            data = publishData
            options { routingKey = "test"; exchange = "test" }
            properties { headers = publishHeaders }
        }
    }

    connection.resources.scope.coroutineContext[Job]!!.join()
}
