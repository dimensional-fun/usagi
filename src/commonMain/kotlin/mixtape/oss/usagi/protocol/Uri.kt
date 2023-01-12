package mixtape.oss.usagi.protocol

import io.ktor.http.*
import mixtape.oss.usagi.tools.AMQP
import mixtape.oss.usagi.tools.AMQPS

public data class Uri(
    val virtualHost: String,
    val username: String,
    val password: String,
    val host: String,
    val port: Int,
    val secure: Boolean
) {
    public companion object {
        public fun fromUrl(url: String): Uri {
            return fromUrl(Url(url))
        }

        public fun fromUrl(url: Url): Uri {
            val normalized = url.normalizeProtocol()

            val vhost = "/"
            return Uri(
                vhost,
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
    }
}
