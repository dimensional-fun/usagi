package dimensional.usagi.channel.event

import dimensional.usagi.channel.Channel

/**
 * Signals that the [channel] has been cancelled.
 */
public data class ChannelClosedEvent(override val channel: Channel) : ChannelEvent
