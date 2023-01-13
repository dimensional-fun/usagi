package dimensional.usagi.connection.frame

import dimensional.usagi.protocol.reader.ProtocolReader
import dimensional.usagi.protocol.writer.ProtocolWriter

public data class FrameHeader(
    val type: FrameType,
    val channel: Int,
    val size: Int = 0,
) {
    public companion object {
        internal suspend fun readFrom(reader: ProtocolReader): FrameHeader {
            return FrameHeader(
                FrameType.readFrom(reader),
                reader.readShortUnsigned().toInt(),
                reader.readLongUnsigned(),
            )
        }
    }

    public suspend fun writeTo(writer: ProtocolWriter) {
        writer.writeByte(type.id.toByte())
        writer.writeShortUnsigned(channel.toShort())
        writer.writeLongUnsigned(size)
    }
}
