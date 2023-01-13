package dimensional.usagi.connection

public class ConnectionPreferencesBuilder {
    /**
     * The max number of channels that can be created by this connection.
     */
    public var maxChannelCount: Int = ConnectionPreferences.DEFAULT_MAX_CHANNEL_COUNT

    /**
     * The max number of bytes that a frame can take up. 0 means no limit.
     */
    public var maxFrameSize: Int = 0

    /**
     * The interval to send heartbeats at, in seconds.
     */
    public var heartbeatInterval: Int = 60

    public fun build(): ConnectionPreferences = ConnectionPreferences(
        maxChannelCount, maxFrameSize, heartbeatInterval
    )
}