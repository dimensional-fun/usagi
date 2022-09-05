package mixtape.oss.usagi.connection.event

import mixtape.oss.usagi.connection.Connection

/**
 * The connection has been blocked for the provided [reason]
 *
 * @param reason The reason the connection is blocked from publishing.
 */
public data class ConnectionBlockedEvent(override val connection: Connection, val reason: String) : ConnectionEvent
