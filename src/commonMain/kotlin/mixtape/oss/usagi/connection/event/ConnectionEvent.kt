package mixtape.oss.usagi.connection.event

import mixtape.oss.usagi.connection.Connection

/**
 * An event relating to an AMQP connection.
 */
public interface ConnectionEvent {
    /**
     * The connection in relation to this event.
     */
    public val connection: Connection
}
