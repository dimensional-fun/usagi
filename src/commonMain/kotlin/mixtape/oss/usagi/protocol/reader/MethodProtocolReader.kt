package mixtape.oss.usagi.protocol.reader

import io.ktor.utils.io.core.*

public class MethodProtocolReader(private val delegate: ProtocolReader) : ProtocolReader {

    /**
     * If we are reading one or more bits, holds the current packed collection of bits
     */
    private var bits = 0

    /**
     * If we are reading one or more bits, keeps track of which bit position we will read from next.
     * (reading least to most significant order)
     */
    private var nextBitMask = 0x100

    /* SCUFFED FUCKING SHIT AHHHHH */
    public suspend fun readBit(): Boolean {
        if (nextBitMask > 0x80) {
            bits = delegate.readOctet()
            nextBitMask = 0x01
        }

        val result = bits and nextBitMask != 0
        nextBitMask = nextBitMask shl 1

        return result
    }

    public fun clearBits() {
        bits = 0
        nextBitMask = 0x100
    }

    /* regular shit */
    override suspend fun readPacket(n: Int): ByteReadPacket {
        clearBits()
        return delegate.readPacket(n)
    }

    override suspend fun readByte(): Byte {
        clearBits()
        return delegate.readByte()
    }

    override suspend fun readFloat(): Float {
        clearBits()
        return delegate.readFloat()
    }

    override suspend fun readDouble(): Double {
        clearBits()
        return delegate.readDouble()
    }

    override suspend fun readShortSigned(): Short {
        clearBits()
        return delegate.readShortSigned()
    }

    override suspend fun readShortUnsigned(): Short {
        clearBits()
        return delegate.readShortUnsigned()
    }

    override suspend fun readLongSigned(): Int {
        clearBits()
        return delegate.readLongSigned()
    }

    override suspend fun readLongUnsigned(): Int {
        clearBits()
        return delegate.readLongUnsigned()
    }

    override suspend fun readLongLongSigned(): Long {
        clearBits()
        return delegate.readLongLongSigned()
    }

    override suspend fun readLongLongUnsigned(): Long {
        clearBits()
        return delegate.readLongLongUnsigned()
    }
}
