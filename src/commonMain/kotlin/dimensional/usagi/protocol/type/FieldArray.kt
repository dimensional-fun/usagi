package dimensional.usagi.protocol.type

import dimensional.usagi.protocol.kxser.AMQDecoder
import dimensional.usagi.protocol.kxser.AMQEncoder
import dimensional.usagi.tools.into
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(with = FieldArray.Serializer::class)
public value class FieldArray(public val array: List<Any?>) {
    public object Serializer : KSerializer<FieldArray> {
        override val descriptor: SerialDescriptor
            get() = buildSerialDescriptor("FieldArray", SerialKind.CONTEXTUAL)

        override fun serialize(encoder: Encoder, value: FieldArray) {
            val amqEncoder = encoder.into<AMQEncoder>()
            amqEncoder.write { writeArray(value.array) }
        }

        override fun deserialize(decoder: Decoder): FieldArray {
            val amqDecoder = decoder.into<AMQDecoder>()
            return FieldArray(amqDecoder.read { readArray() })
        }
    }
}
