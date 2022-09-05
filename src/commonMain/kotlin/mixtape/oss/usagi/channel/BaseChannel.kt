package mixtape.oss.usagi.channel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import mixtape.oss.usagi.channel.command.Command
import mixtape.oss.usagi.channel.command.CommandAssembler
import mixtape.oss.usagi.connection.Connection
import mixtape.oss.usagi.connection.frame.Frame
import mixtape.oss.usagi.protocol.AMQP
import mixtape.oss.usagi.protocol.Method
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.measureTimedValue

public abstract class BaseChannel(
    /**
     * The underlying connection for this channel.
     */
    public val connection: Connection,
    /**
     * This channel's ID number.
     */
    public val id: Int,
) {
    public companion object {
        private val log = KotlinLogging.logger { }
    }

    public val scope: CoroutineScope = CoroutineScope(
        connection.resources.scope.coroutineContext + SupervisorJob() + CoroutineName("Channel $id")
    )

    /** Used for constructing */
    private val assembler = CommandAssembler()
    /** Shared flow of incoming [Command]s */
    private val commandFlow = MutableSharedFlow<Command>()

    /**
     * Opens this channel.
     */
    public suspend fun open() {
        val resp = rpc(AMQP.Channel.Open())
        require(resp.method is AMQP.Channel.OpenOk) {
            "Expected `channel.open-ok`, received: ${resp.method.methodName()}"
        }
    }

    /**
     * Handles an incoming frame to this channel.
     *
     * @return `true` if the frame was handled.
     */
    public suspend fun handleFrame(frame: Frame): Boolean {
        if (frame.header.channel != id) {
            log.trace { "Channel[$id] Requested to handle frame for channel: ${frame.header.channel}" }
            return false
        }

        val completed = assembler.handleFrame(frame)
        if (completed) {
            val command = assembler.assemble()
            handleIncomingCommand(command)
            assembler.reset()
        }

        return true
    }

    /**
     * Used to filter incoming commands passed down to RPC listeners.
     *
     * @param command The incoming command.
     * @return `true` if the command was processed, `false` to continue.
     */
    public abstract suspend fun processIncomingCommand(command: Command): Boolean

    /**
     * Handles an inbound command.
     *
     * @param command The inbound [Command] instance.
     */
    public suspend fun handleIncomingCommand(command: Command) {
        if (processIncomingCommand(command)) {
            return
        }

        log.debug { "[Channel $id] RECV command ${command.method}" }
        commandFlow.emit(command)
    }

    /**
     *
     */
    public suspend fun send(method: Method) {
        val command = Command(method)
        return send(command)
    }

    /**
     *
     */
    public suspend fun send(command: Command): Unit {
        log.debug { "[Channel $id] SEND command ${command.method}" }
        command.transmit(this)
    }

    /**
     *
     */
    public suspend fun rpc(method: Method): Command = rpc(Command(method))

    /**
     *
     */
    public suspend fun rpc(command: Command): Command {
        send(command)
        val (resp, took) = measureTimedValue {
            receiveCommand()
        }

        log.trace { "[Channel $id] RPC took $took" }
        return resp
    }

    /**
     *
     */
    public fun shutdown() {

    }

    public suspend fun receiveCommand(): Command = commandFlow.first()

    public suspend fun receiveCommand(timeout: Duration): Command {
        return withTimeout(timeout) { receiveCommand() }
    }
}
