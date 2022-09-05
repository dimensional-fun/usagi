package mixtape.oss.usagi.connection.event

import mixtape.oss.usagi.connection.Connection

/**
 * The connection has been unblocked
 */
public data class ConnectionUnblockedEvent(override val connection: Connection) : ConnectionEvent
