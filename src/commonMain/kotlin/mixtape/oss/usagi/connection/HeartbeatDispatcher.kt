package mixtape.oss.usagi.connection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mixtape.oss.kyuso.Kyuso
import mixtape.oss.kyuso.task.Task
import mixtape.oss.kyuso.tools.Runnable
import mixtape.oss.kyuso.tools.calculatingDelay
import mixtape.oss.usagi.connection.frame.Frame
import mixtape.oss.usagi.connection.frame.FrameType
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

public class HeartbeatDispatcher(
    public val connection: Connection,
    private val kyuso: Kyuso
) {
    public companion object {
        private val log = KotlinLogging.logger {  }

        private fun now(): Instant = Clock.System.now()
    }

    private var task: Task? = null
    private var lastActivityTime: Instant? = null
    private lateinit var lastHeartbeat: TimeMark

    /** Mutable latency flow, see [Connection.latency] */
    internal val latencyFlow = MutableStateFlow<Duration?>(null)

    /** Signify connection activity */
    public fun active() {
        lastActivityTime = now()
    }

    /** Handle heartbeat acknowledge */
    public suspend fun ack() {
        if (!::lastHeartbeat.isInitialized) {
            return
        }

        latencyFlow.emit(lastHeartbeat.elapsedNow())
        log.debug { "Latest connection latency has been updated: ${latencyFlow.value}" }
    }

    /** Starts dispatching heartbeats to the remote server */
    public fun start(interval: Duration) {
        stop()

        val delay = interval / 2
        task = kyuso.dispatchEvery(
            calculatingDelay(delay),
            runnable = HeartbeatRunnable(delay)
        )
    }

    /** Stops the heart-beat task. */
    public fun stop() {
        task?.cancel()
        task = null
    }

    private inner class HeartbeatRunnable(val delay: Duration) : Runnable<Unit> {
        override suspend fun run() {
            val lat = lastActivityTime
            if (lat == null || now() > (lat - delay)) {
                lastHeartbeat = TimeSource.Monotonic.markNow()
                connection.writeFrame(Frame(FrameType.Heartbeat, 0))
                connection.flush()
            }
        }
    }
}
