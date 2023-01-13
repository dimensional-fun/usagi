package dimensional.usagi.connection

import dimensional.usagi.connection.auth.AuthMechanism
import dimensional.usagi.connection.auth.AuthMechanisms
import dimensional.usagi.connection.event.ConnectionEvent
import dimensional.usagi.protocol.Protocol
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public class ConnectionResourcesBuilder {
    public var dispatcher: CoroutineDispatcher = Dispatchers.Default

    /**
     * The protocol information to use when creating the connection.
     *
     * **Only change this if you know what you're doing.**
     */
    public var protocol: Protocol = Protocol.DEFAULT

    /**
     * Duration to wait after starting a connection handshake.
     */
    public var handshakeTimeout: Duration = 10.seconds

    /**
     * The auth mechanisms to attempt. Defaults to `PLAIN` if none are provided.
     */
    public var authMechanism: List<AuthMechanism>? = null

    /**
     * The preferences to use.
     */
    public var preferences: ConnectionPreferencesBuilder.() -> Unit = {}

    /**
     * The properties of this client.
     */
    public var properties: ClientProperties.() -> Unit = {}

    /**
     *
     */
    public var eventFlow: MutableSharedFlow<ConnectionEvent> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)

    public inline fun preferences(crossinline block: ConnectionPreferencesBuilder.() -> Unit) {
        val old = preferences
        preferences = {
            old()
            block()
        }
    }

    public inline fun properties(crossinline block: ClientProperties.() -> Unit) {
        val old = properties
        properties = {
            old()
            block()
        }
    }

    public fun build(): ConnectionResources = ConnectionResources(
        CoroutineScope(dispatcher + Job()),
        protocol,
        handshakeTimeout,
        authMechanism ?: listOf(AuthMechanisms.Plain),
        ConnectionPreferencesBuilder()
            .apply(preferences)
            .build(),
        ClientProperties()
            .apply(properties)
            .build(),
        eventFlow
    )
}