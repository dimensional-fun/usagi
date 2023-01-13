package dimensional.usagi.protocol.kxser

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.internal.TaggedEncoder

public class AMQFieldMapEncoder : TaggedEncoder<AMQField>() {
    override fun SerialDescriptor.getTag(index: Int): AMQField {
        TODO("Not yet implemented")
    }
}