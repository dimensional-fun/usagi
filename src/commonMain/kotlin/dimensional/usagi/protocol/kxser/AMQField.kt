package dimensional.usagi.protocol.kxser

import dimensional.usagi.protocol.kxser.annotations.AMQBit
import dimensional.usagi.protocol.kxser.annotations.AMQInteger
import dimensional.usagi.protocol.kxser.annotations.AMQString
import kotlinx.serialization.descriptors.SerialDescriptor

public data class AMQField(
    val index: Int,
    val stringType: AMQStringType,
    val intType: AMQIntegerType,
    val intSigned: Boolean,
    val isBit: Boolean,
) {
    internal companion object {
        fun extract(descriptor: SerialDescriptor, config: Amqp.Config, index: Int): AMQField {
            val annotations = descriptor.getElementAnnotations(index)

            /* create tag */
            val int: AMQInteger = annotations.filterIsInstance<AMQInteger>().firstOrNull() ?: config.defaultIntConfig
            return AMQField(
                index,
                annotations.filterIsInstance<AMQString>().firstOrNull()?.type ?: config.defaultStringType,
                int.type,
                int.signed,
                annotations.filterIsInstance<AMQBit>().firstOrNull() != null
            )
        }
    }
}
