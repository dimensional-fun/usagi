package mixtape.oss.usagi.protocol

import io.ktor.http.*
import mixtape.oss.usagi.tools.AMQP

/**
 */
public class URIBuilder(
    public var host: String = "",
    public var port: Int = URLProtocol.AMQP.defaultPort,
    public var username: String = "guest",
    public var password: String = "guest",
    public var virtualHost: String = "/"
)
