package dimensional.usagi.protocol

public data class ProtocolVersion(val major: Int, val minor: Int, val revision: Int = 0) {
    public companion object {
        public val DEFAULT: ProtocolVersion = ProtocolVersion(0, 9, 1)
    }

    /**
     * Whether this [ProtocolVersion] matches the [other].
     *
     * @param other The other version to match against.
     * @return `true` if [other] matches this version.
     */
    public fun matches(other: ProtocolVersion): Boolean {
        val adjustedOther = other.adjust()
        val adjustedThis  = adjust()
        return adjustedOther.minor == adjustedThis.minor && adjustedOther.major == adjustedThis.major
    }

    /**
     * Adjust this version, mainly due to the AMQP 0-8 specification being weird.
     *
     * @return The adjusted [ProtocolVersion]
     */
    public fun adjust(): ProtocolVersion = if (major == 8 && minor == 0) {
        copy(major = minor, minor = major)
    } else {
        this
    }

    override fun toString(): String = "$major-$minor"
}
