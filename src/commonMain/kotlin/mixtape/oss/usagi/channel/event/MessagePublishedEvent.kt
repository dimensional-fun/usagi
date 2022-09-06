package mixtape.oss.usagi.channel.event

import mixtape.oss.usagi.channel.consumer.Consumer
import mixtape.oss.usagi.channel.consumer.Delivery

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
