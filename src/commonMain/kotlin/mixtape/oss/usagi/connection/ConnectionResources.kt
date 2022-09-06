package mixtape.oss.usagi.connection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import mixtape.oss.usagi.connection.auth.AuthMechanism
import mixtape.oss.usagi.connection.auth.AuthMechanisms
import mixtape.oss.usagi.connection.event.ConnectionEvent
import mixtape.oss.usagi.protocol.Protocol
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public data class ConnectionResources(
    val credentials: Pair<String, String>,
    val virtualHost: String = "/",
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + Job()),
    val protocol: Protocol = Protocol.DEFAULT,
    val handshakeTimeout: Duration = 10.seconds,
    val authMechanisms: List<AuthMechanism> = listOf(AuthMechanisms.Plain),
    val preferences: ConnectionPreferences = ConnectionPreferences(),
    val connectionProperties: Map<String, Any> = DEFAULT_CONNECTION_PROPERTIES,
    val eventFlow: MutableSharedFlow<ConnectionEvent> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)
) {
    public companion object {
        public val DEFAULT_CONNECTION_PROPERTIES: Map<String, Any> = mapOf(
            "product" to "usagi",
            "information" to "https://github.com/mixtape-bot/usagi",
            "platform" to "Kotlin ${KotlinVersion.CURRENT}",
            "version" to "0.0.0",
            "copyright" to "Copyright (c) 2019 - 2022 Dimensional Fun",
            "capabilities" to mapOf(
                "authentication_failure_close" to true,
                "basic.nack" to true,
                "connection.blocked" to true,
                "consumer_cancel_notify" to true,
                "exchange_exchange_bindings" to true,
                "per_consumer_qos" to true,
                "publisher_confirms" to true,
            )
        )
    }
}
