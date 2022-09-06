package mixtape.oss.usagi.channel.event

import mixtape.oss.usagi.channel.Channel
import mixtape.oss.usagi.channel.consumer.Consumer

public interface ConsumerEvent : ChannelEvent {
    public val consumer: Consumer

    override val channel: Channel
        get() = consumer.channel
}
