package dimensional.usagi.protocol

import dimensional.usagi.tools.AMQP
import dimensional.usagi.tools.AMQPS
import io.ktor.http.*

public fun Uri(url: String): Uri {
    return Uri(Url(url))
}

public fun Uri(url: Url): Uri {
    val normalized = url.normalizeProtocol()

    /* get vhost from path segments. */
    val vhost = url.pathSegments
        .drop(1)
        .firstOrNull()
        ?: ""

    /* return amqp uri */
    return Uri(
        vhost.ifBlank { "/" },
        normalized.user ?: "guest",
        normalized.password ?: "guest",
        normalized.host,
        normalized.port,
        normalized.protocol == URLProtocol.AMQPS
    )
}

private fun Url.normalizeProtocol(): Url {
    val protocol: URLProtocol = when (protocol.name) {
        "amqp" -> URLProtocol.AMQP
        "amqps" -> URLProtocol.AMQPS
        else -> error("Invalid Protocol: ${protocol.name}")
    }

    val url = URLBuilder(this)
    url.protocol = protocol

    return url.build()
}
