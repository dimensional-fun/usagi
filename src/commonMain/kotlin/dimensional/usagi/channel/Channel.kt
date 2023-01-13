package dimensional.usagi.channel

import dimensional.usagi.channel.command.Command
import dimensional.usagi.channel.consumer.Consumer
import dimensional.usagi.channel.consumer.Delivery
import dimensional.usagi.channel.event.ChannelEvent
import dimensional.usagi.channel.event.MessagePublishedEvent
import dimensional.usagi.connection.Connection
import dimensional.usagi.protocol.AMQP
import dimensional.usagi.tools.into
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import mu.KotlinLogging

public class Channel(
    connection: Connection,
    id: Int
) : BaseChannel(connection, id) {
    public companion object {
        private val log = KotlinLogging.logger {  }
    }

    private val consumerMap = hashMapOf<String, Consumer>()
    private val eventFlow = MutableSharedFlow<ChannelEvent>(extraBufferCapacity = Int.MAX_VALUE)

    /**  */
    public val consumers: Map<String, Consumer> get() = object : Map<String, Consumer> by consumerMap {}
    /**  */
    public val events: SharedFlow<ChannelEvent> get() = eventFlow

    override suspend fun processIncomingCommand(command: Command): Boolean {
        if (command.method is AMQP.Channel.Close) {
            processShutdown(command.method)
            return true
        }

        return when (command.method) {

            /* consumer-specific commands */
            is AMQP.Basic.Deliver -> {
                val consumer = consumerMap[command.method.consumerTag]
                if (consumer != null) {
                    val delivery = Delivery(
                        consumer,
                        Delivery.Envelope(
                            command.method.deliveryTag,
                            command.method.redelivered,
                            command.method.exchange,
                            command.method.routingKey,
                        ),
                        command.header?.properties.into(),
                        command.body!!.asBytes()
                    )

                    eventFlow.emit(MessagePublishedEvent(consumer, delivery))
                }

                true
            }

            is AMQP.Basic.Cancel -> {
                true
            }

            is AMQP.Basic.CancelOk -> {
                !inRPC // forward this to RPC
            }

            is AMQP.Basic.RecoverOk -> {
                !inRPC // forward this to RPC
            }

            /* everything else */
            else -> false
        }
    }

    internal suspend fun createConsumer(method: AMQP.Basic.Consume): Consumer {
        val ok = rpc(method)
        require(ok.method is AMQP.Basic.ConsumeOk) { "Expected `basic.consume-ok`, not ${ok.method.methodName()}" }

        val consumer = Consumer(this, ok.method.consumerTag)
        consumerMap[consumer.tag] = consumer

        return consumer
    }

    private suspend fun processShutdown(method: AMQP.Channel.Close) {
        log.debug { "[Channel $id] Server requested close: $method" }

        try {
            send(AMQP.Channel.CloseOk)
        } finally {
            connection.channels.free(this)
            rpc?.cancel()
        }
    }
}
