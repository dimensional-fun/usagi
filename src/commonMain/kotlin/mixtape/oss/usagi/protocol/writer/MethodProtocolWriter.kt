package mixtape.oss.usagi.protocol.writer

import io.ktor.utils.io.core.*

public class MethodProtocolWriter(private val delegate: ProtocolWriter) : ProtocolWriter {
    private var needBitFlush = false
    private var bitAccumulator: Byte = 0
    private var bitMask = 1

    private fun reset() {
        needBitFlush = false
        bitAccumulator = 0
        bitMask = 1
    }

    override suspend fun flush() {
        if (needBitFlush) {
            delegate.writeByte(bitAccumulator)
            reset()
        }
    }

    public suspend fun writeBit(b: Boolean) {
        if (bitMask > 0x80) {
            flush()
        }

        if (b) {
            bitAccumulator = (bitAccumulator.toInt() or bitMask).toByte()
        }

        bitMask = bitMask shl 1
        needBitFlush = true
    }

    override suspend fun writePacket(value: ByteReadPacket) {
        flush()
        delegate.writePacket(value)
    }

    override suspend fun writeByte(value: Byte) {
        flush()
        delegate.writeByte(value)
    }

    override suspend fun writeFloat(value: Float) {
        flush()
        delegate.writeFloat(value)
    }

    override suspend fun writeDouble(value: Double) {
        flush()
        delegate.writeDouble(value)
    }

    override suspend fun writeShortSigned(value: Short) {
        flush()
        delegate.writeShortSigned(value)
    }

    override suspend fun writeShortUnsigned(value: Short) {
        flush()
        delegate.writeShortUnsigned(value)
    }

    override suspend fun writeLongSigned(value: Int) {
        flush()
        delegate.writeLongSigned(value)
    }

    override suspend fun writeLongUnsigned(value: Int) {
        flush()
        delegate.writeLongUnsigned(value)
    }

    override suspend fun writeLongLongSigned(value: Long) {
        flush()
        delegate.writeLongLongSigned(value)
    }

    override suspend fun writeLongLongUnsigned(value: Long) {
        flush()
        delegate.writeLongLongUnsigned(value)
    }
}
