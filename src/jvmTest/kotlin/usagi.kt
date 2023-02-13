import com.rabbitmq.client.AMQP
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import dimensional.kyuso.Kyuso
import dimensional.kyuso.tools.calculatingDelay
import dimensional.usagi.Usagi
import dimensional.usagi.channel.event.MessageReturnedEvent
import dimensional.usagi.channel.method.*
import dimensional.usagi.channel.on
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
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
    channel.on<MessageReturnedEvent> {
        println(this)
    }

    val kyuso = Kyuso(Dispatchers.IO.limitedParallelism(1))
    kyuso.dispatchEvery(calculatingDelay(1.seconds)) {
        channel.basic.publish {
            data = publishData
            options { routingKey = "test"; exchange = "test"; mandatory = true }
            properties { headers = publishHeaders }
        }
    }

    connection.resources.scope.coroutineContext.job.join()
}
