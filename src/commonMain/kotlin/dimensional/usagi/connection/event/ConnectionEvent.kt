package dimensional.usagi.connection.event

import dimensional.usagi.connection.Connection

/**
 * An event relating to an AMQP connection.
 */
public interface ConnectionEvent {
    /**
     * The connection in relation to this event.
     */
    public val connection: Connection
}
