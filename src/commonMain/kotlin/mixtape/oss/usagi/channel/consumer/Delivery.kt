package mixtape.oss.usagi.channel.consumer

import mixtape.oss.usagi.channel.method.basic
import mixtape.oss.usagi.protocol.AMQP

public data class Delivery(
    val consumer: Consumer,
    val envelope: Envelope,
    val properties: AMQP.Basic.Properties,
    val data: ByteArray
) {
    /**
     *
     */
    public suspend fun ack(
        multiple: Boolean = false
    ) {
        consumer.channel.basic.ack {
            deliveryTag = envelope.deliveryTag
            multiple(multiple)
        }
    }

    /**
     *
     */
    public suspend fun nack(
        multiple: Boolean = false,
        requeue: Boolean = false
    ) {
        consumer.channel.basic.nack {
            deliveryTag = envelope.deliveryTag
            multiple(multiple)
            requeue(requeue)
        }
    }

    public data class Envelope(
        val deliveryTag: Long,
        val redelivered: Boolean,
        val exchange: String,
        val routingKey: String
    )
}
