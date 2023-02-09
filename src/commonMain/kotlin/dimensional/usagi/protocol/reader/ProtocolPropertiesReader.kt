// Some of this code is adapted from
// https://github.com/rabbitmq/rabbitmq-java-client/blob/main/src/main/java/com/rabbitmq/client/impl/ContentHeaderPropertyReader.java

package dimensional.usagi.protocol.reader

public class ProtocolPropertiesReader(reader: ProtocolReader): ProtocolReader by reader {
    private var flagWord = 1
    private var bitCount = 15

    /**
     * Whether the current flag word is a continuation.
     */
    private val isContinuationBitSet: Boolean
        get() = flagWord and 1 != 0

    public suspend fun readPresence(): Boolean {
        if (bitCount == 15) readFlagWord()

        val bit = 15 - bitCount
        bitCount++

        return flagWord and (1 shl bit) != 0
    }

    public fun finishPresence() {
        require (!isContinuationBitSet) {
            "Unexpected continuation flag word"
        }
    }

    private suspend fun readFlagWord() {
        require(isContinuationBitSet) {
            "Attempted to read flag word when none advertised"
        }

        flagWord = readShortSigned().toInt()
        bitCount = 0
    }
}
