package dimensional.usagi.tools

/**
 * The range of the values a UShort can hold.
 */
public val UShort.Companion.RANGE: ClosedRange<Int>
    get() = MIN_VALUE.toInt()..MAX_VALUE.toInt()

/**
 * Copies [size] elements of [src] starting at [srcPos] into [dst] at [dstPos]
 */
public fun arraycopy(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, size: Int) {
    src.copyInto(dst, dstPos, srcPos, srcPos + size)
}

public inline fun <reified T> Any?.intoOrNull(): T? =
    this as? T

public inline fun <reified T> Any?.into(): T {
    return requireNotNull(intoOrNull<T>()) { "Cannot cast ${this?.let { it::class.qualifiedName }} to ${T::class.qualifiedName}" }
}
