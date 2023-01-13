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
    val protocol: Protocol,
    val handshakeTimeout: Duration ,
    val authMechanisms: List<AuthMechanism>,
    val preferences: ConnectionPreferences,
    val clientProperties: Map<String, Any?>,
    val eventFlow: MutableSharedFlow<ConnectionEvent>
)
