package mixtape.oss.usagi.protocol.type

import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import mixtape.oss.usagi.protocol.kxser.AMQDecoder
import mixtape.oss.usagi.protocol.kxser.AMQEncoder
import mixtape.oss.usagi.protocol.reader.ProtocolReader
import mixtape.oss.usagi.tools.into
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(with = LongString.Serializer::class)
public value class LongString(private val value: ByteArray) {
    public companion object {
        public val Empty: LongString = LongString(ByteArray(0))

        public suspend fun readFrom(input: ProtocolReader): LongString {
            val length = input.readLongUnsigned()
            return LongString(input.readPacket(length).readBytes())
        }
    }

    public object Serializer : KSerializer<LongString> {

        override val descriptor: SerialDescriptor
            get() = SerialDescriptor("LongString", ByteArraySerializer().descriptor)

        override fun serialize(encoder: Encoder, value: LongString) {
            encoder.into<AMQEncoder>().write { writeLongString(value) }
        }

        override fun deserialize(decoder: Decoder): LongString {
            val dec = decoder.into<AMQDecoder>()
            return dec.read {
                val len = readLongUnsigned()
                val pak = readPacket(len)
                LongString(pak.readBytes())
            }
        }
    }

    public constructor(value: String) : this(value.encodeToByteArray())

    public val size: Int get() = value.size

    public fun getBytes(): ByteArray = value

    public fun asString(): String = value.decodeToString()
}
