package dimensional.usagi.protocol.writer

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

public val ByteWriteChannel.amqp: ProtocolWriter get() = ChannelProtocolWriter(this)

internal class ChannelProtocolWriter(val output: ByteWriteChannel) : ProtocolWriter {
    override suspend fun writePacket(value: ByteReadPacket) {
        output.writePacket(value)
    }

    override suspend fun writeByte(value: Byte) {
        output.writeByte(value)
    }

    override suspend fun writeFloat(value: Float) {
        output.writeFloat(value)
    }

    override suspend fun writeDouble(value: Double) {
        output.writeDouble(value)
    }

    override suspend fun writeShortSigned(value: Short) {
        output.writeShort(value)
    }

    override suspend fun writeShortUnsigned(value: Short) {
        output.writeShort(value.toUShort().toShort())
    }

    override suspend fun writeLongSigned(value: Int) {
        output.writeInt(value)
    }

    override suspend fun writeLongUnsigned(value: Int) {
        output.writeInt(value.toUInt().toInt())
    }

    override suspend fun writeLongLongSigned(value: Long) {
        output.writeLong(value)
    }

    override suspend fun writeLongLongUnsigned(value: Long) {
        output.writeLong(value.toULong().toLong())
    }
}
