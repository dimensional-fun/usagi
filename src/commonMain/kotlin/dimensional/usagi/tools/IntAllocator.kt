package dimensional.usagi.tools

/**
 * A class for allocating integers from a given range that uses a
 * [BitSet] representation for free integers.
 *
 * See [rabbitmq/rabbitmq-java-client IntAllocator.java](https://github.com/rabbitmq/rabbitmq-java-client/blob/main/src/main/java/com/rabbitmq/utility/IntAllocator.java)
 */
public class IntAllocator(bottom: Int, top: Int) {
    private val range = bottom..top
    private val numberOfBits: Int = range.last - range.first
    private val freeSet = BitSet(numberOfBits)
    private var lastIndex = 0

    init {
        freeSet.set(0, numberOfBits)
    }

    /**
     * Allocate a free integer from the range. Returns -1 if no
     * more integers are available.
     */
    public fun allocate(): Int {
        val setIndex = freeSet.nextSetBit(lastIndex)
            .takeUnless { it < 0 }
            ?: freeSet.nextSetBit(0)

        if (setIndex < 0) {
            return -1
        }

        lastIndex = setIndex
        freeSet.clear(setIndex)

        return setIndex + range.first
    }

    /**
     * Make the provided [reservation ID][reservation] available for allocation again.
     *
     * @param reservation The previously allocated integer to free.
     */
    public fun free(reservation: Int) {
        freeSet.set(reservation - range.first)
    }

    /**
     * Attempts to reserve the provided ID as if it had been allocated. Returns
     * true if it is available, false otherwise.
     *
     * @param reservation The integer to be allocated, if possible.
     */
    public fun reserve(reservation: Int): Boolean {
        val idx = reservation - range.first

        val free = freeSet.get(idx)
        if (free) {
            freeSet.clear(idx)
        }

        return free
    }
}
