package dimensional.usagi.protocol

import dimensional.usagi.tools.AMQP
import io.ktor.http.*

/**
 */
public class URIBuilder(
    public var host: String = "",
    public var port: Int = URLProtocol.AMQP.defaultPort,
    public var username: String = "guest",
    public var password: String = "guest",
    public var virtualHost: String = "/"
)
