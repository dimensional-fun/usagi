package mixtape.oss.usagi.tools

/**
 * Simplified version of java.util.BitSet
 */
public actual class BitSet actual constructor(size: Int) {
    public companion object {
        private const val ADDRESS_BITS_PER_WORD = 6
        private const val BITS_PER_WORD = 1 shl ADDRESS_BITS_PER_WORD
        private const val WORD_MASK = -0x1L
        
        private fun wordIndex(bitIndex: Int): Int = bitIndex shr ADDRESS_BITS_PER_WORD
    }

    private var words = LongArray(size)
    private var wordsInUse: Int = 0

    private fun ensureCapacity(s: Int) {
        val present = this.words.size
        if (present < s) {
            /* allocate new array */
            val new = LongArray(maxOf(2 * words.size, s))
            words.copyInto(new)
            words = new
        }
    }

    private fun checkInvariants() {
        assert(wordsInUse == 0 || words[wordsInUse - 1] != 0L)
        assert(wordsInUse >= 0 && wordsInUse <= words.size)
        assert(wordsInUse == words.size || words[wordsInUse] == 0L)
    }

    private fun recalculateWordsInUse() {
        var i: Int = wordsInUse - 1
        while (i >= 0) {
            if (words[i] != 0L) break
            i--
        }

        wordsInUse = i + 1
    }

    private fun expandTo(wordIndex: Int) {
        val required = wordIndex + 1
        if (wordsInUse < required) {
            ensureCapacity(required)
            wordsInUse = required
        }
    }

    public actual fun set(bitIndex: Int) {
        val wordIndex: Int = wordIndex(bitIndex)
        expandTo(wordIndex)
        words[wordIndex] = words[wordIndex] or (1L shl bitIndex) // Restores invariants
        checkInvariants()
    }

    public actual fun set(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        val startWordIndex: Int = wordIndex(fromIndex)
        val endWordIndex: Int = wordIndex(toIndex - 1)
        expandTo(endWordIndex)

        val firstWordMask: Long = WORD_MASK shl fromIndex
        val lastWordMask: Long = WORD_MASK ushr -toIndex
        if (startWordIndex == endWordIndex) {
            words[startWordIndex] = words[startWordIndex] or (firstWordMask and lastWordMask)
        } else {
            words[startWordIndex] = words[startWordIndex] or firstWordMask
            for (i in startWordIndex + 1 until endWordIndex) words[i] = WORD_MASK
            words[endWordIndex] = words[endWordIndex] or lastWordMask
        }

        checkInvariants()
    }

    public actual fun get(bitIndex: Int): Boolean {
        checkInvariants()
        val wordIndex: Int = wordIndex(bitIndex)
        return wordIndex < wordsInUse && words[wordIndex] and (1L shl bitIndex) != 0L
    }

    public actual fun nextSetBit(fromIndex: Int): Int {
        checkInvariants()

        var u: Int = wordIndex(fromIndex)
        if (u >= wordsInUse) {
            return -1
        }

        var word = words[u] and (WORD_MASK shl fromIndex)
        while (true) {
            if (word != 0L) return u * BITS_PER_WORD + word.countTrailingZeroBits()
            if (++u == wordsInUse) return -1
            word = words[u]
        }
    }

    public actual fun nextClearBit(fromIndex: Int): Int {
        checkInvariants()

        var u: Int = wordIndex(fromIndex)
        if (u >= wordsInUse) {
            return fromIndex
        }

        var word = words[u].inv() and (WORD_MASK shl fromIndex)
        while (true) {
            if (word != 0L) return u * BITS_PER_WORD + word.countTrailingZeroBits()
            if (++u == wordsInUse) return wordsInUse * BITS_PER_WORD
            word = words[u].inv()
        }
    }

    public actual fun clear(bitIndex: Int) {
        val wordIndex: Int = wordIndex(bitIndex)
        if (wordIndex >= wordsInUse) {
            return
        }

        words[wordIndex] = words[wordIndex] and (1L shl bitIndex).inv()

        recalculateWordsInUse()
        checkInvariants()
    }
}
