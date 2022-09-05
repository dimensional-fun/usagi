package mixtape.oss.usagi.protocol.writer

import io.ktor.utils.io.core.*
import kotlinx.datetime.Instant
import mixtape.oss.usagi.protocol.type.LongString
import mixtape.oss.usagi.tools.into

public interface ProtocolWriter {
    public suspend fun writeOctet(value: Int) {
        writeByte((value and 0xFF).toByte())
    }

    public suspend fun writeShortString(value: String) {
        require(value.length in 0..255)

        val bytes = value.encodeToByteArray()
        writeOctet(bytes.size)
        writePacket(ByteReadPacket(bytes))
    }

    public suspend fun writeLongString(value: LongString) {
        writeLongSigned(value.size)
        if (value.size > 0) {
            val packet = ByteReadPacket(value.getBytes())
            writePacket(packet)
        }
    }

    public suspend fun writeChar(value: Char) {
        writeByte(value.code.toByte())
    }

    public suspend fun writeFieldTable(value: Map<String, Any?>) {
        val packet = BytePacketBuilder()
        for ((k, v) in value) {
            packet.amqp.writeShortString(k)
            packet.amqp.writeFieldValue(v)
        }

        writeLongUnsigned(packet.size)
        writePacket(packet.build())
    }

    public suspend fun writeArray(value: List<Any?>) {
        val packet = BytePacketBuilder()
        for (element in value) writeFieldValue(element)

        writeLongSigned(packet.size)
        writePacket(packet.build())
    }

    public suspend fun writeFieldValue(value: Any?) {
        when (value) {
            is Boolean -> {
                writeChar('t')
                writeOctet(if (value) 1 else 0)
            }

            is Short -> {
                writeChar('U')
                writeShortSigned(value)
            }

            is Int -> {
                writeChar('I')
                writeLongSigned(value)
            }

            is Long -> {
                writeChar('L')
                writeLongLongSigned(value)
            }

            is Float -> {
                writeChar('f')
                writeFloat(value)
            }

            is Double -> {
                writeChar('d')
                writeDouble(value)
            }

            is String -> {
                writeChar('S')
                writeLongString(LongString(value))
            }

            is LongString -> {
                writeChar('S')
                writeLongString(value)
            }

            is List<*> -> {
                writeChar('A')
                writeArray(value)
            }

            is Instant -> {
                writeChar('T')
                writeTimestamp(value)
            }

            is Map<*, *> -> {
                val table = value.into<Map<String, Any?>>()
                writeChar('F')
                writeFieldTable(table)
            }

            null -> {
                writeChar('V')
            }

            else -> error("Unsupported field value: $value")
        }
    }

    public suspend fun writeTimestamp(value: Instant) {
        writeLongLongUnsigned(value.epochSeconds)
    }

    public suspend fun writePacket(value: ByteReadPacket)

    public suspend fun writeByte(value: Byte)

    public suspend fun writeFloat(value: Float)

    public suspend fun writeDouble(value: Double)

    public suspend fun writeShortSigned(value: Short)

    public suspend fun writeShortUnsigned(value: Short)

    public suspend fun writeLongSigned(value: Int)

    public suspend fun writeLongUnsigned(value: Int)

    public suspend fun writeLongLongSigned(value: Long)

    public suspend fun writeLongLongUnsigned(value: Long)

    public suspend fun flush() {}
}
