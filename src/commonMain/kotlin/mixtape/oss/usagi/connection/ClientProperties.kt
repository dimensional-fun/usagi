package mixtape.oss.usagi.connection

public class ClientProperties {
    public companion object {
        /** The capabilities of this AMQP client */
        public val CAPABILITIES: Map<String, Boolean> = mapOf(
            "authentication_failure_close" to true,
            "basic.nack" to true,
            "connection.blocked" to true,
            "consumer_cancel_notify" to true,
            "exchange_exchange_bindings" to true,
            "per_consumer_qos" to true,
            "publisher_confirms" to true,
        )
    }

    public var product: String = "usagi"

    public var information: String = "https://github.com/mixtape-bot/usagi"

    public var platform: String = "Kotlin ${KotlinVersion.CURRENT}"

    public var version: String = "0.0.0"

    public var copyright: String = "Copyright (c) 2019 - 2022 Dimensional Fun"

    public var connectionName: String? = null

    public val extra: MutableMap<String, Any?> = mutableMapOf()

    public fun build(): Map<String, Any?> {
        val properties: MutableMap<String, Any?> = (extra + mapOf<String, Any?>(
            "product" to product,
            "information" to information,
            "platform" to platform,
            "version" to version,
            "copyright" to copyright,
            "capabilities" to CAPABILITIES,
        )).toMutableMap()

        if (connectionName != null) {
            properties["connection_name"] = connectionName
        }

        return properties
    }
}
