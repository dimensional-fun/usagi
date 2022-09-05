package mixtape.oss.usagi.protocol

public data class Protocol(
    val version: ProtocolVersion
) {
    public companion object {
        public val DEFAULT: Protocol = Protocol(ProtocolVersion.DEFAULT)

        private val AMQP get() = "AMQP".encodeToByteArray()

        private fun createHeader(protocol: Protocol): ByteArray {
            val (major, minor, rev) = protocol.version
            return AMQP + byteArrayOf(0, major.toByte(), minor.toByte(), rev.toByte())
        }
    }

    /**
     * The sequence of bytes used to start a new AMQP connection.
     * See AMQP 0.9.1 specification 4.2.2
     */
    public val header: ByteArray = createHeader(this)
}
