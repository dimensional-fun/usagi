package mixtape.oss.usagi.connection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import mixtape.oss.usagi.connection.auth.AuthMechanism
import mixtape.oss.usagi.connection.auth.AuthMechanisms
import mixtape.oss.usagi.connection.event.ConnectionEvent
import mixtape.oss.usagi.protocol.Protocol
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
