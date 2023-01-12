import com.rabbitmq.client.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import mixtape.oss.kyuso.Kyuso
import mixtape.oss.kyuso.tools.calculatingDelay
import mixtape.oss.usagi.Usagi
import mixtape.oss.usagi.channel.event.MessagePublishedEvent
import mixtape.oss.usagi.channel.consumer.on
import mixtape.oss.usagi.channel.method.basic
import mixtape.oss.usagi.channel.method.exchange
import mixtape.oss.usagi.channel.method.queue
import mixtape.oss.usagi.connection.Connection
import mixtape.oss.usagi.connection.ConnectionResources
import kotlin.time.Duration.Companion.milliseconds

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
    kyuso.dispatchEvery(calculatingDelay(20.milliseconds)) {
        val properties = AMQP.BasicProperties.Builder()
            .headers(publishHeaders)
            .build()

        channel.basicPublish("test", "test", properties, publishData)
    }

    kyuso.scope.coroutineContext.job.join()
}

suspend fun usagi() {
    val connection = Usagi.connect(
        "amqp://localhost"
    )

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

//    val kyuso = Kyuso(Dispatchers.IO.limitedParallelism(1))
//    kyuso.dispatchEvery(calculatingDelay(20.milliseconds)) {
//        channel.basic.publish {
//            data = publishData
//            options { routingKey = "test"; exchange = "test" }
//            properties { headers = publishHeaders }
//        }
//    }

    connection.resources.scope.coroutineContext[Job]!!.join()
}
