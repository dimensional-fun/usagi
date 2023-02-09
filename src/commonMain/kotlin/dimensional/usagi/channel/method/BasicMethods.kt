package dimensional.usagi.channel.method

import dimensional.usagi.annotations.InternalUsagiAPI
import dimensional.usagi.channel.command.Command
import dimensional.usagi.channel.command.ContentBody
import dimensional.usagi.channel.command.ContentHeader
import dimensional.usagi.channel.consumer.Consumer
import dimensional.usagi.protocol.AMQP

/**
 * @param method
 */
@OptIn(InternalUsagiAPI::class)
public suspend fun BasicMethods.consume(method: AMQP.Basic.Consume): Consumer {
    return channel.createConsumer(method)
}

public suspend fun BasicMethods.consume(
    block: AMQP.Basic.Consume.Builder.() -> Unit
): Consumer {
    return consume(AMQP.Basic.Consume(block))
}


/**
 * Publish a message
 *
 * @param block Function used for building the message to publish.
 */
public suspend fun BasicMethods.publish(
    block: PublicationBuilder.() -> Unit,
) {
    val command = PublicationBuilder()
        .apply(block)
        .build()

    channel.send(command)
}


public class PublicationBuilder {
    private var properties: AMQP.Basic.Properties.Builder = AMQP.Basic.Properties.Builder()

    private var options: AMQP.Basic.Publish.Builder = AMQP.Basic.Publish.Builder()

    public lateinit var data: ByteArray

    public fun properties(block: AMQP.Basic.Properties.Builder.() -> Unit): PublicationBuilder {
        properties.block()
        return this
    }

    public fun options(block: AMQP.Basic.Publish.Builder.() -> Unit): PublicationBuilder {
        options.block()
        return this
    }

    public fun build(): Command {
        val header = ContentHeader(
            data.size.toLong(),
            properties.build()
        )

        return Command(options.build(), header, ContentBody.Whole(data))
    }
}