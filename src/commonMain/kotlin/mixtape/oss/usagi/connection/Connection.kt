package mixtape.oss.usagi.connection

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import mixtape.oss.kyuso.Kyuso
import mixtape.oss.kyuso.task.Task
import mixtape.oss.usagi.channel.BaseChannel
import mixtape.oss.usagi.channel.ChannelManager
import mixtape.oss.usagi.channel.command.Command
import mixtape.oss.usagi.connection.event.ConnectionBlockedEvent
import mixtape.oss.usagi.connection.event.ConnectionEvent
import mixtape.oss.usagi.connection.event.ConnectionUnblockedEvent
import mixtape.oss.usagi.connection.frame.Frame
import mixtape.oss.usagi.connection.frame.FrameTooLargeException
import mixtape.oss.usagi.connection.frame.FrameType
import mixtape.oss.usagi.protocol.AMQP
import mixtape.oss.usagi.protocol.ProtocolVersion
import mixtape.oss.usagi.protocol.reader.amqp
import mixtape.oss.usagi.protocol.type.LongString
import mixtape.oss.usagi.protocol.writer.amqp
import mu.KotlinLogging
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

public data class Connection(
    private val socket: Socket,
    internal val resources: ConnectionResources,
) {
    public companion object {
        private val log = KotlinLogging.logger { }

        /**
         *
         */
        public suspend fun connect(
            remote: SocketAddress,
            resources: ConnectionResources,
        ): Connection {
            /* create a new TCP socket connection to the specified remote address */
            val (connection, took) = measureTimedValue {
                val socket = aSocket(SelectorManager(resources.scope.coroutineContext))
                    .tcp()
                    .connect(remote)

                /* create a new AMQP connection instance and negotiate with remote. */
                val connection = Connection(socket, resources)
                connection.initialize()
            }

            log.debug { "Initialized connection in $took" }
            return connection
        }
    }

    /** The task used to read incoming frames from [input], see [startFrameTask] */
    private var frameTask: Task? = null
    /** Read channel for incoming frames */
    private val input = socket.openReadChannel()
    /** Write channel for outgoing frames */
    private val output = socket.openWriteChannel()
    /** Used to schedule and dispatch asynchronous tasks. */
    private val kyuso = Kyuso(resources.scope)
    /** Sends heart-beat frames to the remote. */
    private val heartbeatDispatcher = HeartbeatDispatcher(this, kyuso)
    /** Special channel just for this connection used for housekeeping. */
    private val channel0 = object : BaseChannel(this@Connection, 0) {
        override suspend fun processIncomingCommand(command: Command): Boolean = when (command.method) {
            is AMQP.Connection.Close -> {
                log.debug { "Server requested close: ${command.method}" }
                send(AMQP.Connection.CloseOk())
                frameTask?.cancel()
                heartbeatDispatcher.stop()
                socket.close()
                true
            }

            is AMQP.Connection.Blocked -> {
                val event = ConnectionBlockedEvent(connection, command.method.reason)
                resources.eventFlow.emit(event)
                true
            }

            is AMQP.Connection.Unblocked -> {
                val event = ConnectionUnblockedEvent(connection)
                resources.eventFlow.emit(event)
                true
            }

            else -> false
        }
    }

    /**
     * The current latency of this connection.
     */
    public val latency: StateFlow<Duration?> get() = heartbeatDispatcher.latencyFlow

    /**
     * The current preferences for this connection.
     */
    public var preferences: ConnectionPreferences = resources.preferences

    /**
     * Events related to this connection.
     */
    public val events: SharedFlow<ConnectionEvent> get() = resources.eventFlow

    /**
     * Whether this connection has a limited frame size.
     */
    public val hasLimitedFrameSize: Boolean get() = preferences.hasLimitedFrameSize

    /**
     * The channel manager for this connection.
     */
    public val channels: ChannelManager = ChannelManager(this)

    /**
     * Flushes the byte write stream for this connection.
     */
    public fun flush() {
        output.flush()
    }

    /**
     * Writes the supplied [frame] to this connection.
     *
     * @param frame The [Frame] instance to write.
     * @throws FrameTooLargeException If [frame] exceeds the max frame size.
     */
    public suspend fun writeFrame(frame: Frame) {
        if (preferences.exceedsFrameMax(frame.size)) {
            throw FrameTooLargeException(frame, preferences.maxFrameSize)
        }

        frame.writeTo(output.amqp)
        log.trace { "[Channel ${frame.header.channel}] >>> $frame" }
        heartbeatDispatcher.active()
    }

    /**
     * Suspends until a single frame from this connection has been read.
     *
     * @throws FrameTooLargeException If it exceeds the max frame size.
     */
    public suspend fun readFrame(): Frame {
        // todo: is this fine?
        if (input.availableForRead < 1) {
            input.awaitContent()
        }

        /* read a frame from the input channel. */
        val frame = Frame.readFrom(input.amqp)
        if (preferences.exceedsFrameMax(frame.size)) {
            /* exceeds negotiated max frame size */
            throw FrameTooLargeException(frame, preferences.maxFrameSize)
        }

        return frame
    }

    /**
     * Initializes this AMQP connection.
     */
    public suspend fun initialize(): Connection {
        /* write the protocol frame header. */
        output.writeFully(resources.protocol.header)
        flush()

        /* start connection pre-start sequence. */
        val start: AMQP.Connection.Start
        var tune: AMQP.Connection.Tune? = null

        /* start the frame read loop. */
        startFrameTask()

        try {
            // receive inbound start command
            start = channel0.receiveCommand(resources.handshakeTimeout).method as AMQP.Connection.Start

            // detect client-server protocol version mismatch.
            val serverVersion = ProtocolVersion(start.versionMajor, start.versionMinor)
            require(serverVersion.matches(resources.protocol.version)) {
                // TODO: replace with custom exception
                "Protocol version mismatch: expected ${resources.protocol.version}, got $serverVersion"
            }

            // find a compatible auth mechanism
            val serverAuthMechanisms = start.mechanisms
                .asString()
                .split(' ')

            val authMechanism = resources.authMechanisms.find { it.name in serverAuthMechanisms }
                ?: error("No compatible auth mechanisms found, server offered: $serverAuthMechanisms.")

            /* start authentication sequence */
            val (username, password) = resources.credentials // TODO: support dynamic credentials

            var challenge: LongString? = null
            while (tune == null) {
                val startOk = AMQP.Connection.StartOk {
                    if (challenge == null) {
                        clientProperties = resources.connectionProperties
                        mechanism = authMechanism.name
                    }

                    response = authMechanism.handleChallenge(challenge, username, password)
                }

                val response = channel0.rpc(startOk)
                when (response.method) {
                    is AMQP.Connection.Tune -> {
                        tune = response.method
                    }

                    is AMQP.Connection.Secure -> {
                        log.trace { "Remote is requesting secure..." }
                        challenge = response.method.challenge
                    }

                    else -> error("Unexpected command method: ${response.method}")
                }
            }
        } catch (ex: Exception) {
            throw ex
        }

        /* negotiate the connection preferences. */
        preferences = preferences.negotiate(tune)

        channel0.send(preferences.tuneOk())

        /* start heart-beating. */
        heartbeatDispatcher.start(preferences.heartbeat.seconds)

        /* send connection.open and wait for ok */
        val ok = channel0.rpc(AMQP.Connection.Open(resources.virtualHost, "", false)).method
        require(ok is AMQP.Connection.OpenOk) {
            "Expected connection.open-ok but got: $ok"
        }

        return this
    }

    private suspend fun startFrameTask() {
        frameTask = kyuso.dispatch {
            while (coroutineContext.isActive) {
                try {
                    val frame = readFrame()
                    if (frame.header.type is FrameType.Heartbeat) {
                        heartbeatDispatcher.ack()
                        continue
                    }

                    log.trace { "[Channel ${frame.header.channel}] <<< $frame" }

                    /* handle the frame according to the channel */
                    if (frame.header.channel == 0) {
                        channel0.handleFrame(frame)
                    } else {
                        val channel = channels.get(frame.header.channel)
                        if (channel == null) {
                            log.debug { "Received a frame for an unknown channel, ignoring..." }
                            continue
                        }

                        channel.handleFrame(frame)
                    }
                } catch (ex: Throwable) {
                    log.error(ex) { "Encountered exception while in frame loop." }
                    break
                }
            }
        }
    }
}
