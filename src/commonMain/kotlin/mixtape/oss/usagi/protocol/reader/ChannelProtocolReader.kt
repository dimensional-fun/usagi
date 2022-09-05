package mixtape.oss.usagi.protocol.reader

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

public val ByteReadChannel.amqp: ProtocolReader get() = ChannelProtocolReader(this)

internal class ChannelProtocolReader(val channel: ByteReadChannel) : ProtocolReader {
    override suspend fun readPacket(n: Int): ByteReadPacket = channel.readPacket(n)

    override suspend fun readByte(): Byte     = channel.readByte()
    override suspend fun readFloat(): Float   = channel.readFloat()
    override suspend fun readDouble(): Double = channel.readDouble()

    override suspend fun readShortSigned(): Short     = read(2) { readShortSigned() }
    override suspend fun readShortUnsigned(): Short   = read(2) { readShortUnsigned() }

    override suspend fun readLongSigned(): Int        = read(4) { readLongSigned() }
    override suspend fun readLongUnsigned(): Int      = read(4) { readLongUnsigned() }

    override suspend fun readLongLongSigned(): Long   = read(8) { readLongLongSigned() }
    override suspend fun readLongLongUnsigned(): Long = read(8) { readLongLongUnsigned() }

    private suspend inline fun <T> read(count: Int, block: ProtocolReader.() -> T): T {
        val packet = channel.readPacket(count)
        return packet.amqp.block()
    }
}
