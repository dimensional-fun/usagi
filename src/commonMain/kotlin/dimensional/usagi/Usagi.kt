package dimensional.usagi

import dimensional.usagi.connection.Connection
import dimensional.usagi.connection.ConnectionResources
import dimensional.usagi.protocol.Uri

public object Usagi {
    public suspend fun connect(uri: String): Connection {
        val uri = Uri.fromUrl(uri)
        return Connection.connect(uri, ConnectionResources())
    }
}
