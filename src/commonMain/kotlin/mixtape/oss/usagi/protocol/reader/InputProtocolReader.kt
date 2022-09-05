package mixtape.oss.usagi.protocol.reader

import io.ktor.utils.io.core.*

public val Input.amqp: ProtocolReader get() = InputProtocolReader(this)

internal class InputProtocolReader(val input: Input) : ProtocolReader {
    override suspend fun readPacket(n: Int): ByteReadPacket = ByteReadPacket(input.readBytes(n))

    override suspend fun readByte(): Byte = input.readByte()

    override suspend fun readFloat(): Float   = input.readFloat()

    override suspend fun readDouble(): Double = input.readDouble()

    override suspend fun readShortSigned(): Short     = input.readShort()

    override suspend fun readShortUnsigned(): Short   = input.readUShort().toShort()

    override suspend fun readLongSigned(): Int        = input.readInt()

    override suspend fun readLongUnsigned(): Int      = input.readUInt().toInt()

    override suspend fun readLongLongSigned(): Long   = input.readLong()

    override suspend fun readLongLongUnsigned(): Long = input.readULong().toLong()
}
