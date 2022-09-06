package mixtape.oss.usagi.connection

import mixtape.oss.usagi.channel.BaseChannel
import mixtape.oss.usagi.channel.command.Command
import mixtape.oss.usagi.connection.event.ConnectionBlockedEvent
import mixtape.oss.usagi.connection.event.ConnectionUnblockedEvent
import mixtape.oss.usagi.protocol.AMQP
import mu.KotlinLogging

internal class Channel0(connection: Connection) : BaseChannel(connection, 0) {
    companion object {
        val log = KotlinLogging.logger {  }
    }

    override suspend fun processIncomingCommand(command: Command): Boolean = if (connection.running) {
        when (command.method) {
            is AMQP.Connection.Close -> {
                log.debug { "Server requested close: ${command.method}" }
                connection.handleConnectionClose(command.method)
                true
            }

            is AMQP.Connection.Blocked -> {
                val event = ConnectionBlockedEvent(connection, command.method.reason)
                connection.resources.eventFlow.emit(event)
                true
            }

            is AMQP.Connection.Unblocked -> {
                val event = ConnectionUnblockedEvent(connection)
                connection.resources.eventFlow.emit(event)
                true
            }

            else -> false
        }
    } else {
        when (command.method) {
            is AMQP.Connection.Close -> {
                send(AMQP.Connection.CloseOk())
                true
            }

            is AMQP.Connection.CloseOk -> {
                connection.running = false
                !inRPC
            }

            else -> true
        }
    }
}
