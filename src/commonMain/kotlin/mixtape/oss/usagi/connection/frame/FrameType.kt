package mixtape.oss.usagi.connection.frame

import mixtape.oss.usagi.protocol.reader.ProtocolReader

/** must be in this order, see [id] */
public sealed class FrameType(public val id: Int) {
    public companion object {
        private val ALL: List<FrameType> = listOf(Method, Header, Body, Heartbeat)

        /**
         *
         */
        public suspend fun readFrom(reader: ProtocolReader): FrameType {
            val id = reader.readOctet()
            return ALL.find { it.id == id } ?: Unknown(id)
        }
    }

    public object Method : FrameType(1) {
        override fun toString(): String = "FrameType::Method"
    }

    public object Header : FrameType(2) {
        override fun toString(): String = "FrameType::Header"
    }

    public object Body : FrameType(3) {
        override fun toString(): String = "FrameType::Body"
    }

    public object Heartbeat : FrameType(8) {
        override fun toString(): String = "FrameType::Heartbeat"
    }

    public class Unknown(id: Int) : FrameType(id) {
        override fun toString(): String = "FrameType::Unknown(id=$id)"
    }
}
