package mixtape.oss.usagi.channel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mixtape.oss.usagi.channel.command.Command
import mixtape.oss.usagi.channel.command.CommandAssembler
import mixtape.oss.usagi.connection.Connection
import mixtape.oss.usagi.connection.frame.Frame
import mixtape.oss.usagi.protocol.AMQP
import mixtape.oss.usagi.protocol.Method
import mixtape.oss.usagi.tools.measure
import mu.KotlinLogging
import kotlin.time.Duration

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

    public val scope: CoroutineScope = connection.resources.scope + SupervisorJob() + CoroutineName("Channel $id")

    /** Used for constructing */
    private val assembler = CommandAssembler()
    /** Shared flow of incoming [Command]s */
    private val commandFlow = MutableSharedFlow<Command>()
    /** Whether sending of content-frames is being done, see [AMQP.Channel.Flow] */
    private var active: Boolean = true
    /** Mutex used for sending commands */
    private val mutex = Mutex()

    /** The current RPC being performed */
    internal var rpc: Deferred<Command>? = null
    /** Whether this channel is performing an RPC call */
    internal val inRPC: Boolean get() = rpc != null

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

    public suspend fun send(method: Method) {
        return quiescingSend(method)
    }

    public suspend fun send(command: Command) {
        return quiescingSend(command)
    }

    public suspend fun rpc(method: Method): Command {
        return rpc(Command(method))
    }

    /**
     *
     */
    public suspend fun rpc(command: Command): Command {
        rpc = scope.async { receiveCommand() }
        send(command)

        return log.measure("[Channel $id] RPC took", log::debug) { rpc!!.await() }
    }

    public fun shutdown() {

    }

    public suspend fun receiveCommand(): Command {
        return commandFlow.first()
    }

    public suspend fun receiveCommand(timeout: Duration): Command {
        return withTimeout(timeout) { receiveCommand() }
    }

    /**
     *
     */
    private suspend fun quiescingSend(method: Method) {
        return quiescingSend(Command(method))
    }

    private suspend fun quiescingSend(command: Command) {
        mutex.withLock {
            if (command.method.hasContent()) {
                while (!active) {
                    // todo: ensure that the channel is open
                }
            }

            log.debug { "[Channel $id] SEND command ${command.method}" }
            command.transmit(this)
        }
    }
}
