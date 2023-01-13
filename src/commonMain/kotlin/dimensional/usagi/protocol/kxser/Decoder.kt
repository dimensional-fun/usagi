package dimensional.usagi.protocol.kxser

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.TaggedDecoder

public open class Decoder(public val amqp: Amqp) : TaggedDecoder<AMQField>() {
    private var currentIndex: Int = -1

    override fun decodeSequentially(): Boolean = true

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        if (currentIndex++ > descriptor.elementsCount) CompositeDecoder.DECODE_DONE
        else currentIndex

    override fun SerialDescriptor.getTag(index: Int): AMQField = AMQField.extract(this, amqp.config, index)
}