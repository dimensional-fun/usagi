package mixtape.oss.usagi.protocol

import io.ktor.utils.io.core.*
import mixtape.oss.usagi.connection.frame.Frame
import mixtape.oss.usagi.connection.frame.FrameHeader
import mixtape.oss.usagi.connection.frame.FrameType
import mixtape.oss.usagi.protocol.reader.MethodProtocolReader
import mixtape.oss.usagi.protocol.reader.amqp
import mixtape.oss.usagi.protocol.writer.MethodProtocolWriter
import mixtape.oss.usagi.protocol.writer.amqp

public abstract class Method {
    public companion object {
        public suspend fun fromFrame(frame: Frame): Method {
            require(frame.header.type is FrameType.Method) {
                "Frame type must be FrameType.Header, not ${frame.header.type}"
            }

            val payload = ByteReadPacket(frame.body)
            return AMQP.readMethodFrom(MethodProtocolReader(payload.amqp))
        }
    }

    /**
     * The AMQP class id of this method.
     */
    public abstract fun classId(): Short

    /**
     * The AMQP method id of this method.
     */
    public abstract fun methodId(): Short

    /**
     * The AMQP method name of this method.
     */
    public abstract fun methodName(): String

    /**
     * Whether content is present within this method.
     */
    internal abstract fun hasContent(): Boolean

    /**
     *
     */
    internal abstract suspend fun writeTo(writer: MethodProtocolWriter)

    /**
     *
     */
    internal suspend fun asFrame(channel: Int): Frame {
        val packet = BytePacketBuilder()
        packet.amqp.writeShortSigned(classId())
        packet.amqp.writeShortSigned(methodId())

        val writer = MethodProtocolWriter(packet.amqp)
        writeTo(writer)
        writer.flush()

        return Frame(
            FrameHeader(FrameType.Method, channel, packet.size),
            packet.build().readBytes()
        )
    }
}
