package mixtape.oss.usagi.protocol.type

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import mixtape.oss.usagi.protocol.kxser.AMQDecoder
import mixtape.oss.usagi.protocol.kxser.AMQEncoder
import mixtape.oss.usagi.tools.into
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(with = FieldTable.Serializer::class)
public value class FieldTable(public val value: Map<String, Any?>) {
    public object Serializer : KSerializer<FieldTable> {
        override val descriptor: SerialDescriptor
            get() = buildSerialDescriptor("FieldTable", SerialKind.CONTEXTUAL)

        override fun serialize(encoder: Encoder, value: FieldTable) {
            encoder.into<AMQEncoder>().write { writeFieldTable(value.value) }
        }

        override fun deserialize(decoder: Decoder): FieldTable {
            return FieldTable(decoder.into<AMQDecoder>().read {
                readFieldTable()
            })
        }
    }
}
