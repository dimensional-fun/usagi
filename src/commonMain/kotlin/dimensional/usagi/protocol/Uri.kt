package dimensional.usagi.protocol

public data class Uri(
    val virtualHost: String,
    val username: String,
    val password: String,
    val host: String,
    val port: Int,
    val secure: Boolean
)
