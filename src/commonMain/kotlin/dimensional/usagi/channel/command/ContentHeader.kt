package dimensional.usagi.channel.command

import dimensional.usagi.connection.frame.Frame
import dimensional.usagi.connection.frame.FrameHeader
import dimensional.usagi.connection.frame.FrameType
import dimensional.usagi.protocol.AMQP
import dimensional.usagi.protocol.reader.ProtocolPropertiesReader
import dimensional.usagi.protocol.reader.ProtocolReader
import dimensional.usagi.protocol.writer.ProtocolPropertiesWriter
import dimensional.usagi.protocol.writer.amqp
import io.ktor.utils.io.core.*

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
