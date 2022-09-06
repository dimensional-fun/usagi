package mixtape.oss.usagi.channel.command

import mixtape.oss.usagi.connection.frame.Frame
import mixtape.oss.usagi.connection.frame.FrameType
import mixtape.oss.usagi.tools.arraycopy

public sealed class ContentBody {
    /**
     *
     */
    public abstract fun asBytes(): ByteArray

    /**
     */
    public class Whole(private val bytes: ByteArray) : ContentBody() {
        override fun asBytes(): ByteArray = bytes
    }

    /**
     */
    public data class Fragmented(private val totalSize: Long) : ContentBody() {
        private val fragments = mutableListOf<ByteArray>()

        /**
         * Whether the content body has been completed.
         */
        public val completed: Boolean
            get() = currentSize.toLong() == totalSize

        /**
         * The current size of the content body.
         */
        internal val currentSize: Int
            get() = fragments.fold(0) { cs, frag -> cs + frag.size }

        /**
         * Handle a received content body frame.
         *
         * @param frame The content body frame to handle, throws [IllegalStateException] otherwise.
         * @return `true` if all fragments have been collected.
         */
        internal fun handle(frame: Frame): Boolean {
            require(frame.header.type is FrameType.Body) {
                "Expected frame type to be FrameType.Body, not ${frame.header.type}"
            }

            fragments += frame.body
            return completed
        }

        /**
         * Joins the body fragments into one [ByteArray] of the [totalSize]
         */
        public override fun asBytes(): ByteArray {
            require(completed) {
                "Content body has not been completed."
            }

            return ByteArray(totalSize.toInt()).also { body ->
                var offset = 0
                for (fragment in fragments) {
                    arraycopy(fragment, 0, body, offset, fragment.size)
                    offset += fragment.size
                }
            }
        }
    }

}
