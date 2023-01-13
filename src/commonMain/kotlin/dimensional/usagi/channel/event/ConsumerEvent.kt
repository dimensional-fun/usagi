package dimensional.usagi.channel.event

import dimensional.usagi.channel.Channel
import dimensional.usagi.channel.consumer.Consumer

public interface ConsumerEvent : ChannelEvent {
    public val consumer: Consumer

    override val channel: Channel
        get() = consumer.channel
}
