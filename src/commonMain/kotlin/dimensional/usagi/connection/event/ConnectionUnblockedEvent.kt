package dimensional.usagi.connection.event

import dimensional.usagi.connection.Connection

/**
 * The connection has been unblocked
 */
public data class ConnectionUnblockedEvent(override val connection: Connection) : ConnectionEvent
