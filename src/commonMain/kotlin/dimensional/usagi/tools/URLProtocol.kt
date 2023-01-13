package dimensional.usagi.tools

import io.ktor.http.*

/** AMQP with port 5672 */
public val URLProtocol.Companion.AMQP: URLProtocol
    get() = URLProtocol("amqp", 5672)

/** secure AMQP with port 5671 */
public val URLProtocol.Companion.AMQPS: URLProtocol
    get() = URLProtocol("amqps", 5671)
