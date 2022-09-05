package mixtape.oss.usagi.channel

import mixtape.oss.usagi.channel.command.Command
import mixtape.oss.usagi.channel.consumer.Consumer
import mixtape.oss.usagi.connection.Connection
import mixtape.oss.usagi.protocol.AMQP
import mu.KotlinLogging

public class Channel(
    connection: Connection,
    id: Int
) : BaseChannel(connection, id) {
    public companion object {
        private val log = KotlinLogging.logger {  }
    }

    private val consumerMap = hashMapOf<String, Consumer>()

    public val consumers: Map<String, Consumer> get() = object : Map<String, Consumer> by consumerMap {}

    override suspend fun processIncomingCommand(command: Command): Boolean = when (command.method) {
        // TODO: handle channel related shit here.
        /* channel-specific commands. */
        is AMQP.Channel.Close -> {
            log.warn { "[Channel $id] Server requested close..." }
            true
        }

        /* consumer-specific commands */
        is AMQP.Basic.Deliver -> {
            consumerMap[command.method.consumerTag]?.handle(command.method, command)
            true
        }

        is AMQP.Basic.Cancel -> {
            consumerMap[command.method.consumerTag]?.handle(command.method, command)
            true
        }

        is AMQP.Basic.CancelOk -> {
            consumerMap[command.method.consumerTag]?.handle(command.method, command)
            false // forward this to RPC
        }

        is AMQP.Basic.RecoverOk -> {
            for (consumer in consumerMap.values) consumer.handle(command.method, command)

            false // forward this to RPC
        }

        /* everything else */
        else -> false
    }

    internal suspend fun createConsumer(method: AMQP.Basic.Consume): Consumer {
        val ok = rpc(method)
        require(ok.method is AMQP.Basic.ConsumeOk) { "Expected `basic.consume-ok`, not ${ok.method.methodName()}" }

        val consumer = Consumer(this, ok.method.consumerTag)
        consumerMap[consumer.tag] = consumer
        consumer.handle(ok.method, ok)

        return consumer
    }
}
