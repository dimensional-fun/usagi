package dimensional.usagi.protocol

import dimensional.usagi.protocol.reader.ProtocolReader
import io.ktor.utils.io.core.*
import kotlin.jvm.JvmInline

@JvmInline
public value class LongString(private val value: ByteArray) {
    public companion object {
        public val Empty: LongString = LongString(ByteArray(0))

        public suspend fun readFrom(input: ProtocolReader): LongString {
            val length = input.readLongUnsigned()
            return LongString(input.readPacket(length).readBytes())
        }
    }

    public constructor(value: String) : this(value.encodeToByteArray())

    public val size: Int get() = value.size

    public fun getBytes(): ByteArray = value

    public fun asString(): String = value.decodeToString()
}
