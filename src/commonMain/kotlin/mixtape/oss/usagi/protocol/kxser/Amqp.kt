package mixtape.oss.usagi.protocol.kxser

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import mixtape.oss.usagi.protocol.kxser.annotations.AMQInteger
import mixtape.oss.usagi.protocol.reader.MethodProtocolReader
import mixtape.oss.usagi.protocol.writer.MethodProtocolWriter

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
