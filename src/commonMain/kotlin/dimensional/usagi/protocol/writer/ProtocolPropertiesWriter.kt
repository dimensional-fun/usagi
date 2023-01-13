package dimensional.usagi.protocol.writer

public class ProtocolPropertiesWriter(writer: ProtocolWriter): ProtocolWriter by writer {
    private var flagWord = 0
    private var bitCount = 0

    private suspend fun emitFlagWord(continuationBit: Boolean) {
        writeShortSigned((if (continuationBit) flagWord or 1 else flagWord).toShort())
        flagWord = 0
        bitCount = 0
    }

    public suspend fun writePresence(present: Boolean) {
        if (bitCount == 15) {
            emitFlagWord(true)
        }

        if (present) {
            flagWord = flagWord or (1 shl (15 - bitCount))
        }

        bitCount++
    }

    public suspend fun finishPresence() {
        emitFlagWord(false)
    }
}
