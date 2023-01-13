package dimensional.usagi.protocol.kxser

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.internal.TaggedDecoder

public class AMQFieldMapDecoder : TaggedDecoder<AMQField>() {
    override fun decodeSequentially(): Boolean = true

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        TODO("Not yet implemented")
    }

    override fun SerialDescriptor.getTag(index: Int): AMQField {
        TODO("Not yet implemented")
    }
}