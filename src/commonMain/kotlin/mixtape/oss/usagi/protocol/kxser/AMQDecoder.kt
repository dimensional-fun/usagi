package mixtape.oss.usagi.protocol.kxser

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.TaggedDecoder
import kotlinx.serialization.modules.SerializersModule
import mixtape.oss.usagi.protocol.reader.MethodProtocolReader

public class AMQDecoder(
    public val amqp: Amqp,
    @PublishedApi
    internal val reader: MethodProtocolReader,
) : TaggedDecoder<AMQField>() {
    override val serializersModule: SerializersModule
        get() = amqp.serializersModule

    private var currentIndex = -1

    override fun decodeSequentially(): Boolean = true

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT -> this
        else -> throw SerializationException("Unable to decode structure kind '${descriptor.kind}'")
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        if (currentIndex++ > descriptor.elementsCount) CompositeDecoder.DECODE_DONE
        else currentIndex

    public inline fun <T> read(crossinline block: suspend MethodProtocolReader.() -> T): T =
        runBlocking { block(reader) }

    override fun decodeTaggedByte(tag: AMQField): Byte = read {
        readByte()
    }

    override fun decodeTaggedChar(tag: AMQField): Char = read {
        readChar()
    }

    override fun decodeTaggedDouble(tag: AMQField): Double = read {
        readDouble()
    }

    override fun decodeTaggedFloat(tag: AMQField): Float = read {
        readFloat()
    }

    override fun decodeTaggedInt(tag: AMQField): Int = read {
        if (tag.intSigned) readLongSigned() else readLongUnsigned()
    }

    override fun decodeTaggedLong(tag: AMQField): Long = read {
        if (tag.intSigned) readLongLongSigned() else readLongLongUnsigned()
    }

    override fun decodeTaggedShort(tag: AMQField): Short = read {
        if (tag.intSigned) readShortSigned() else readShortUnsigned()
    }

    override fun decodeTaggedString(tag: AMQField): String = read {
        if (tag.stringType == AMQStringType.LongString) readLongString().asString() else readShortString()
    }

    override fun decodeTaggedBoolean(tag: AMQField): Boolean = read {
        if (tag.isBit) readBit() else (readByte() == 1.toByte())
    }

    override fun SerialDescriptor.getTag(index: Int): AMQField = AMQField.extract(this, amqp.config, index)
}
