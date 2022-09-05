package mixtape.oss.usagi.connection.frame

import io.ktor.utils.io.core.*
import mixtape.oss.usagi.protocol.reader.ProtocolReader
import mixtape.oss.usagi.protocol.writer.ProtocolWriter

public data class Frame(
    val header: FrameHeader,
    private val payload: ByteArray? = null,
) {
    public companion object {
        public val EMPTY_PAYLOAD: ByteArray = ByteArray(0)

        /**
         * Used to detect the end of a frame.
         */
        public const val END: Int = 0xCE

        /**
         * The size of an empty frame, works out to FRAME_TYPE (1) + FRAME_CHANNEL (2) + FRAME_SIZE (4) + [] (1)
         */
        public const val EMPTY_SIZE: Int = 8

        internal suspend fun readFrom(reader: ProtocolReader): Frame {
            val header = FrameHeader.readFrom(reader)

            /* read the frame payload. */
            val data = reader.readPacket(header.size)

            /* look for the frame-end. */
            val end = reader.readByte()
            require (end == END.toByte()) {
                "Expected frame-end byte, got: $end"
            }

            return Frame(header, data.readBytes())
        }
    }

    public constructor(type: FrameType, channel: Int) : this(FrameHeader(type, channel))

    public val body: ByteArray
        get() = if (header.size != 0) requireNotNull(payload) { "Missing frame payload" } else EMPTY_PAYLOAD

    public val size: Int
        get() = EMPTY_SIZE + (payload?.size ?: 0)

    public suspend fun writeTo(writer: ProtocolWriter) {
        header.writeTo(writer)
        if (payload != null) {
            val packet = ByteReadPacket(payload)
            writer.writePacket(packet)
        }

        writer.writeByte(END.toByte())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Frame
        if (header != other.header) return false
        if (!payload.contentEquals(other.payload)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
