package dimensional.usagi.protocol.kxser

import dimensional.usagi.protocol.reader.MethodProtocolReader
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule

public class AMQDecoder(
    amqp: Amqp,
    @PublishedApi
    internal val reader: MethodProtocolReader,
) : Decoder(amqp) {
    override val serializersModule: SerializersModule
        get() = amqp.serializersModule

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT -> this
        StructureKind.MAP -> AMQFieldMapDecoder()
        else -> throw SerializationException("Unable to decode structure kind '${descriptor.kind}'")
    }

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

}
