package dimensional.usagi.channel.event

import dimensional.usagi.channel.consumer.Consumer
import dimensional.usagi.channel.consumer.Delivery

/**
 * Emitted whenever a message has been published.
 *
 * @param consumer The consumer
 * @param delivery The delivered message.
 */
public data class MessagePublishedEvent(
    override val consumer: Consumer,
    val delivery: Delivery
) : ConsumerEvent
