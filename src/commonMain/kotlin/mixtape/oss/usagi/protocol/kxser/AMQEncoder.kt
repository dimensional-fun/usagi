package mixtape.oss.usagi.protocol.kxser

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.TaggedEncoder
import kotlinx.serialization.modules.SerializersModule
import mixtape.oss.usagi.protocol.kxser.annotations.AMQBit
import mixtape.oss.usagi.protocol.kxser.annotations.AMQInteger
import mixtape.oss.usagi.protocol.kxser.annotations.AMQString
import mixtape.oss.usagi.protocol.type.LongString
import mixtape.oss.usagi.protocol.writer.MethodProtocolWriter

public class AMQEncoder(
    public val amqp: Amqp,
    @PublishedApi
    internal val writer: MethodProtocolWriter,
) : TaggedEncoder<AMQField>() {
    override val serializersModule: SerializersModule
        get() = amqp.serializersModule

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT -> this

        else -> throw SerializationException("Unable to decode structure kind '${descriptor.kind}'")
    }

    public inline fun write(crossinline block: suspend MethodProtocolWriter.() -> Unit) {
        runBlocking { block(writer) }
    }

    override fun encodeTaggedByte(tag: AMQField, value: Byte): Unit = write { writeByte(value) }

    override fun encodeTaggedChar(tag: AMQField, value: Char): Unit = write { writeChar(value) }

    override fun encodeTaggedDouble(tag: AMQField, value: Double): Unit = write { writeDouble(value) }

    override fun encodeTaggedFloat(tag: AMQField, value: Float): Unit = write { writeFloat(value) }

    override fun encodeTaggedInt(tag: AMQField, value: Int): Unit = write {
        if (tag.intSigned) writeLongSigned(value) else writeLongUnsigned(value)
    }

    override fun encodeTaggedLong(tag: AMQField, value: Long): Unit = write {
        if (tag.intSigned) writeLongLongSigned(value) else writeLongLongUnsigned(value)
    }

    override fun encodeTaggedShort(tag: AMQField, value: Short): Unit = write {
        if (tag.intSigned) writeShortSigned(value) else writeShortUnsigned(value)
    }

    override fun encodeTaggedString(tag: AMQField, value: String): Unit = write {
        if (tag.stringType == AMQStringType.LongString) writeLongString(LongString(value)) else writeShortString(value)
    }

    override fun encodeTaggedBoolean(tag: AMQField, value: Boolean): Unit = write {
        if (tag.isBit) writeBit(value) else writeByte(if (value) 0x1 else 0x0)
    }

    override fun SerialDescriptor.getTag(index: Int): AMQField {
        return AMQField.extract(this, amqp.config, index)
    }
}
