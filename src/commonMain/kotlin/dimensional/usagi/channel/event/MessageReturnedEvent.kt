package dimensional.usagi.channel.event

import dimensional.usagi.channel.Channel
import dimensional.usagi.protocol.AMQP

/**
 * A message was returned to the publisher.
 *
 * @see [AMQP.Basic.Return]
 */
public data class MessageReturnedEvent(
    override val channel: Channel,
    val data: AMQP.Basic.Return,
    val body: ByteArray?,
    val properties: AMQP.Basic.Properties
) : ChannelEvent {
    /**
     * The exchange the message was published to.
     */
    val exchange: String
        get() = data.exchange

    /**
     * The routing key used when the message was published.
     */
    val routingKey: String
        get() = data.routingKey
}
