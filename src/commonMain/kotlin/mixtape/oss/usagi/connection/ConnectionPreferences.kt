package mixtape.oss.usagi.connection

import mixtape.oss.usagi.protocol.AMQP
import mixtape.oss.usagi.tools.RANGE

/**
 * Preferences for an [Connection]
 */
public data class ConnectionPreferences(
    /**
     * The max number of channels that can be created by this connection.
     */
    val maxChannelCount: Int = UShort.RANGE.endInclusive - 1,
    /**
     * The max number of bytes that a frame can take up.
     */
    val maxFrameSize: Int = 0,
    /**
     * The interval to send heartbeats at, in seconds.
     */
    val heartbeat: Int = 60
) {
    public companion object {
        /**
         * The default max number of channels that can be allocated
         * to a single connection.
         */
        public const val DEFAULT_MAX_CHANNEL_COUNT: Int = 2047

        /**
         * Negotiate between two values, the preferred [client] value
         * and the [server] value.
         *
         * @param client The value the client prefers, or 0 if
         *               it doesn't matter.
         *
         * @param server The value the server prefers, or 0 if
         *               it doesn't matter.
         */
        private fun negotiate(client: Int, server: Int): Int =
            if (client == 0 || server == 0) maxOf(client, server) else minOf(client, server)

    }

    /**
     * Whether this connection has a limited frame size.
     */
    public val hasLimitedFrameSize: Boolean get() = maxFrameSize > 0

    /**
     * Check whether a frame exceeds the negotiated max size.
     *
     * @param size The size of the frame.
     * @return `true` if [size] exceeds [maxFrameSize] or if
     *   [hasLimitedFrameSize] is true.
     */
    public fun exceedsFrameMax(size: Int): Boolean = if (hasLimitedFrameSize) size > maxFrameSize else false

    /**
     * The [AMQP.Connection.TuneOk] instance for these preferences.
     */
    public fun tuneOk(): AMQP.Connection.TuneOk = AMQP.Connection.TuneOk(
        maxChannelCount.toShort(),
        maxFrameSize,
        heartbeat.toShort()
    )

    public fun negotiate(tune: AMQP.Connection.Tune): ConnectionPreferences {
        /* negotiate channel count */
        val channelCount = negotiate(
            maxChannelCount,
            tune.channelMax.toInt()
        )
        require(channelCount in UShort.RANGE) {
            "Max channel count is not within range: ${UShort.RANGE}"
        }

        /* negotiate frame size */
        val frameSize = negotiate(maxFrameSize, tune.frameMax)

        /* negotiate heartbeat interval */
        val heartbeat = negotiate(
            heartbeat,
            tune.heartbeat.toInt()
        )

        require(heartbeat in UShort.RANGE) {
            "Heartbeat interval is not within range: ${UShort.RANGE}"
        }

        return ConnectionPreferences(
            channelCount,
            frameSize,
            heartbeat
        )
    }
}
