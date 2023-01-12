package mixtape.oss.usagi

import mixtape.oss.usagi.connection.Connection
import mixtape.oss.usagi.connection.ConnectionResources
import mixtape.oss.usagi.protocol.Uri

public object Usagi {
    public suspend fun connect(uri: String): Connection {
        val uri = Uri.fromUrl(uri)
        return Connection.connect(uri, ConnectionResources())
    }
}
