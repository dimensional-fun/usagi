package mixtape.oss.usagi.connection

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import mixtape.oss.kyuso.Kyuso
import mixtape.oss.kyuso.task.Task
import mixtape.oss.usagi.channel.ChannelManager
import mixtape.oss.usagi.connection.event.ConnectionEvent
import mixtape.oss.usagi.connection.frame.Frame
import mixtape.oss.usagi.connection.frame.FrameTooLargeException
import mixtape.oss.usagi.connection.frame.FrameType
import mixtape.oss.usagi.protocol.AMQP
import mixtape.oss.usagi.protocol.Method
import mixtape.oss.usagi.protocol.ProtocolVersion
import mixtape.oss.usagi.protocol.reader.amqp
import mixtape.oss.usagi.protocol.type.LongString
import mixtape.oss.usagi.protocol.writer.amqp
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

public class Connection(private val socket: Socket, internal val resources: ConnectionResources) {
    public companion object {
        private val log = KotlinLogging.logger { }

        /**
         *
         */
        public suspend fun connect(
            brokerAddress: SocketAddress,
            resources: ConnectionResources,
        ): Connection {
            /* create a new TCP socket connection to the specified broker address */
            val (connection, took) = measureTimedValue {
                val socket = aSocket(SelectorManager(resources.scope.coroutineContext))
                    .tcp()
                    .connect(brokerAddress)

                /* create a new AMQP connection instance and negotiate with the broker. */
                val connection = Connection(socket, resources)
                connection.initialize()
            }

            log.debug { "Initialized connection in $took" }
            return connection
        }
    }

    /** The cancellation exception */
    private var cancelCause: ConnectionCancelledException? = null
    /** The task used to read incoming frames from [input], see [startFrameTask] */
    private var frameTask: Task? = null
    /** Read channel for incoming frames */
    private val input = socket.openReadChannel()
    /** Write channel for outgoing frames */
    private val output = socket.openWriteChannel()
    /** Used to schedule and dispatch asynchronous tasks. */
    private val kyuso = Kyuso(resources.scope)
    /** Sends heart-beat frames to the broker. */
    private val heartbeatDispatcher = HeartbeatDispatcher(this, kyuso)

    /** Whether this connection is currently running */
    internal var running: Boolean = false
    /** Special channel just for this connection used for housekeeping. */
    internal val channel0 = Channel0(this)

    /**
     * Whether this connection is open.
     */
    public val isOpen: Boolean get() = running

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

    public fun ensureOpen() {
        if (cancelCause != null) throw cancelCause as Throwable
    }

    public suspend fun shutdown(reason: Method) {
        heartbeatDispatcher.stop()
    }

    /**
     *
     */
    public suspend fun close(replyCode: Short = 200, replyText: String = "OK") {
        ensureOpen()
        val method = AMQP.Connection.Close {
            classId = 0
            methodId = 0
            replyCode(replyCode)
            replyText(replyText)
        }

        cancelCause = ConnectionCancelledException(method, null, true)
        channel0.send(method)
        // connection gets disposed by channel 0
    }

    /**
     * Flushes the byte write stream for this connection.
     */
    public fun flush() {
        ensureOpen()
        output.flush()
    }

    /**
     * Writes the supplied [frame] to this connection.
     *
     * @param frame The [Frame] instance to write.
     * @throws FrameTooLargeException If [frame] exceeds the max frame size.
     */
    public suspend fun writeFrame(frame: Frame) {
        ensureOpen()
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
        ensureOpen()

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
        running = true

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
                        log.trace { "Broker is requesting secure..." }
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

    /** Handle a close method */
    internal suspend fun handleConnectionClose(method: AMQP.Connection.Close) {
        channel0.send(AMQP.Connection.CloseOk())
        cancelCause = ConnectionCancelledException(method, null, false)
        dispose()
    }

    private suspend fun dispose() {
        println("lmfao disposing")
        try {
            channels.shutdown()
            heartbeatDispatcher.stop()
            frameTask?.cancel()
            socket.close()
        } finally {
            resources.scope.cancel(cancelCause)
            resources.scope.coroutineContext[Job]!!.join()
        }
    }

    private suspend fun handleException(ex: Throwable) {
    }

    private suspend fun startFrameTask() {
        frameTask = kyuso.dispatch {
            while (running) {
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
                } catch (ex: CancellationException) {

                } catch (ex: Throwable) {
                    log.error(ex) { "Encountered exception while in frame loop." }
                    handleException(ex)
                    break
                }
            }
        }
    }
}
