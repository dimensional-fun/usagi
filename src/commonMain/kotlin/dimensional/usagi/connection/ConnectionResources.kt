package dimensional.usagi.connection

import dimensional.usagi.connection.auth.AuthMechanism
import dimensional.usagi.connection.auth.AuthMechanisms
import dimensional.usagi.connection.event.ConnectionEvent
import dimensional.usagi.protocol.Protocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public data class ConnectionResources(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + Job()),
    val protocol: Protocol = Protocol.DEFAULT,
    val handshakeTimeout: Duration = 10.seconds,
    val authMechanisms: List<AuthMechanism> = listOf(AuthMechanisms.Plain),
    val preferences: ConnectionPreferences = ConnectionPreferences(),
    val clientProperties: ClientProperties = ClientProperties(),
    val eventFlow: MutableSharedFlow<ConnectionEvent> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)
)
