package mixtape.oss.usagi.channel.command

import io.ktor.utils.io.core.*
import mixtape.oss.usagi.connection.frame.Frame
import mixtape.oss.usagi.connection.frame.FrameHeader
import mixtape.oss.usagi.connection.frame.FrameType
import mixtape.oss.usagi.protocol.AMQP
import mixtape.oss.usagi.protocol.reader.ProtocolPropertiesReader
import mixtape.oss.usagi.protocol.reader.ProtocolReader
import mixtape.oss.usagi.protocol.writer.ProtocolPropertiesWriter
import mixtape.oss.usagi.protocol.writer.amqp

public data class ContentHeader(
    public val bodySize: Long,
    public val properties: Properties
) {
    public companion object {
        public suspend operator fun invoke(reader: ProtocolReader): ContentHeader {
            val classId = reader.readShortUnsigned()
            reader.readShortUnsigned() // weight - unused

            return ContentHeader(
                reader.readLongLongUnsigned(),
                AMQP.readPropertiesFrom(classId, ProtocolPropertiesReader(reader))
            )
        }
    }

    public interface Properties {
        public fun classId(): Short

        public suspend fun writeTo(writer: ProtocolPropertiesWriter)
    }

    internal suspend fun asFrame(channel: Int): Frame {
        val packet = BytePacketBuilder()
        packet.amqp.writeShortUnsigned(properties.classId().toUShort().toShort())
        packet.amqp.writeShortUnsigned(0)
        packet.amqp.writeLongLongUnsigned(bodySize)
        properties.writeTo(ProtocolPropertiesWriter(packet.amqp))

        return Frame(
            FrameHeader(FrameType.Header, channel, packet.size),
            packet.build().readBytes()
        )
    }
}
