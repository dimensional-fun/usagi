package mixtape.oss.usagi.tools

public expect class BitSet public constructor(size: Int) {

    public fun set(bitIndex: Int)

    public fun set(fromIndex: Int, toIndex: Int)

    public fun get(bitIndex: Int): Boolean

    public fun clear(bitIndex: Int)

    public fun nextSetBit(fromIndex: Int): Int

    public fun nextClearBit(fromIndex: Int): Int

}
