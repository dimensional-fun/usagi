package dimensional.usagi.protocol.reader

import dimensional.usagi.protocol.LongString
import io.ktor.utils.io.core.*
import kotlinx.datetime.Instant

public interface ProtocolReader {
    public suspend fun readOctet(): Int {
        return readByte().toInt() and 0xFF
    }

    public suspend fun readChar(): Char {
        return readByte().toInt().toChar()
    }

    public suspend fun readFieldTable(): Map<String, Any?> {
        val packet = readPacket(n = readLongUnsigned())

        /* read k/v pairs from packet */
        val table = mutableMapOf<String, Any?>()
        while (packet.isNotEmpty) {
            val key = packet.amqp.readShortString()
            table[key] = packet.amqp.readFieldValue()
        }

        return table
    }

    public suspend fun readArray(): List<Any?> {
        val packet = readPacket(n = readLongSigned())

        /* read array values from the packet */
        val array = mutableListOf<Any?>()
        while (packet.isNotEmpty) {
            array += packet.amqp.readFieldValue()
        }

        return array
    }

    public suspend fun readFieldValue(): Any? {
        return when (val type = readChar()) {
            't' -> readOctet() == 1
            'b' -> readOctet()
            'B' -> readOctet().toUInt().toInt()
            'U' -> readShortSigned()
            'u' -> readShortUnsigned()
            'I' -> readLongSigned()
            'i' -> readLongUnsigned()
            'L' -> readLongLongSigned()
            'l' -> readLongLongUnsigned()
            'f' -> readFloat()
            'd' -> readDouble()
            'D' -> TODO()
            's' -> readShortString()
            'S' -> readLongString()
            'A' -> readArray()
            'T' -> readTimestamp()
            'F' -> readFieldTable()
            'V' -> null
            else -> error("Unknown field value type: $type")
        }
    }

    public suspend fun readTimestamp(): Instant {
        return Instant.fromEpochSeconds(readLongLongUnsigned())
    }

    public suspend fun readShortString(): String {
        val length = readOctet()
        return readPacket(length).readText()
    }

    public suspend fun readLongString(): LongString {
        return LongString.readFrom(this)
    }
    
    public suspend fun readPacket(n: Int): ByteReadPacket

    public suspend fun readByte(): Byte

    public suspend fun readFloat(): Float

    public suspend fun readDouble(): Double

    public suspend fun readShortSigned(): Short

    public suspend fun readShortUnsigned(): Short

    public suspend fun readLongSigned(): Int

    public suspend fun readLongUnsigned(): Int

    public suspend fun readLongLongSigned(): Long

    public suspend fun readLongLongUnsigned(): Long
}
