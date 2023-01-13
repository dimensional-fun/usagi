package dimensional.usagi.protocol.kxser

import dimensional.usagi.protocol.kxser.annotations.AMQInteger
import dimensional.usagi.protocol.reader.MethodProtocolReader
import dimensional.usagi.protocol.writer.MethodProtocolWriter
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

public open class Amqp(internal val config: Config) : SerialFormat {
    public companion object Default : Amqp(Config())

    override val serializersModule: SerializersModule
        get() = config.serializersModule

    public fun <T> encode(
        serializer: SerializationStrategy<T>,
        value: T,
        writer: MethodProtocolWriter,
        flush: Boolean = true,
    ) {
        serializer.serialize(AMQEncoder(this, writer), value)
        if (flush) runBlocking { writer.flush() }
    }

    public fun <T> decode(
        deserializer: DeserializationStrategy<T>,
        reader: MethodProtocolReader,
    ): T = deserializer.deserialize(AMQDecoder(this, reader))

    public class Config {
        public var serializersModule: SerializersModule = EmptySerializersModule()

        public var defaultStringType: AMQStringType = AMQStringType.ShortString

        public var defaultIntConfig: AMQInteger = AMQInteger(AMQIntegerType.Default, true)
    }
}

public inline fun <reified T : Any> Amqp.encode(value: T, writer: MethodProtocolWriter) {
    encode(serializer(), value, writer)
}

public inline fun <reified T : Any> Amqp.decode(reader: MethodProtocolReader): T {
    return decode(serializer(), reader)
}
