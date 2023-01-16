package dimensional.usagi.channel.consumer

import dimensional.usagi.channel.Channel
import dimensional.usagi.channel.method.basic
import dimensional.usagi.protocol.AMQP

public data class Delivery(
    val consumer: Consumer,
    val envelope: Envelope,
    val properties: AMQP.Basic.Properties,
    val data: ByteArray
) {
    /**
     * The consumer this message came from.
     */
    val channel: Channel
        get() = consumer.channel

    /**
     * Acknowledges this message.
     *
     * @param multiple Whether to acknowledge all messages up to this message.
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
     * Reject one or more received messages.
     *
     * @param multiple Whether to reject all messages up to this one.
     * @param requeue Whether the rejected message(s) should be re-queued instead of discarded/dead-lettered
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
