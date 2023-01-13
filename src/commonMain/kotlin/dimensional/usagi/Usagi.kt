package dimensional.usagi

import dimensional.usagi.connection.Connection
import dimensional.usagi.connection.ConnectionResources
import dimensional.usagi.connection.ConnectionResourcesBuilder
import dimensional.usagi.protocol.Uri
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public suspend inline fun Usagi(
    uri: String,
    build: ConnectionResourcesBuilder.() -> Unit = {}
): Connection = Usagi(Uri(uri), build)

public suspend inline fun Usagi(
    uri: Uri,
    build: ConnectionResourcesBuilder.() -> Unit = {}
): Connection {
    contract {
        callsInPlace(build, InvocationKind.EXACTLY_ONCE)
    }

    val resources = ConnectionResourcesBuilder()
        .apply(build)
        .build()

    return Connection.connect(uri, resources)
}
