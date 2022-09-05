package mixtape.oss.usagi.protocol.writer

import io.ktor.utils.io.core.*

public val Output.amqp: ProtocolWriter get() = OutputProtocolWriter(this)

internal class OutputProtocolWriter(val output: Output) : ProtocolWriter {
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
        output.writeUShort(value.toUShort())
    }

    override suspend fun writeLongSigned(value: Int) {
        output.writeInt(value)
    }

    override suspend fun writeLongUnsigned(value: Int) {
        output.writeUInt(value.toUInt())
    }

    override suspend fun writeLongLongSigned(value: Long) {
        output.writeLong(value)
    }

    override suspend fun writeLongLongUnsigned(value: Long) {
        output.writeULong(value.toULong())
    }
}
