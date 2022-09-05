package mixtape.oss.usagi.channel.command

import mixtape.oss.usagi.channel.BaseChannel
import mixtape.oss.usagi.channel.Channel
import mixtape.oss.usagi.protocol.Method
import mixtape.oss.usagi.connection.frame.Frame
import mixtape.oss.usagi.connection.frame.FrameHeader
import mixtape.oss.usagi.connection.frame.FrameType
import mixtape.oss.usagi.tools.arraycopy

/**
 *
 */
public data class Command(
    /**
     * The method within this command.
     */
    val method: Method,
    /**
     * The content header within this command.
     */
    val header: ContentHeader? = null,
    /**
     * The body for this command.
     */
    val body: ContentBody? = null,
) {
    /**
     * Sends this command to the underlying connection of [channel].
     * It's possible that this command could be sent in multiple frames.
     *
     * @param channel The channel on which to transmit the command.
     */
    public suspend fun transmit(channel: BaseChannel) {
        if (header == null || body == null) {
            /* only send the method frame. */
            val methodFrame = method.asFrame(channel.id)
            channel.connection.writeFrame(methodFrame)
        } else {
            /* needs to be split into multiple messages. */
            val headerFrame = header.asFrame(channel.id)

            /* send the method frame and header frame. */
            channel.connection.writeFrame(method.asFrame(channel.id))
            channel.connection.writeFrame(headerFrame)

            /* split the body into multiple frames if required. */
            val body = body.asBytes()
            if (channel.connection.preferences.hasLimitedFrameSize) {
                val chunkSize = channel.connection.preferences.maxFrameSize - Frame.EMPTY_SIZE
                for (offset in body.indices step chunkSize) {
                    val remaining = body.size - offset

                    /* copy a chunk of the command data into the chunk buffer. */
                    val chunkBuffer = ByteArray(remaining)
                    arraycopy(body, offset, chunkBuffer, 0, remaining)

                    /* write a frame with the contents of the chunk buffer. */
                    val frame = Frame(
                        FrameHeader(FrameType.Body, channel.id, remaining),
                        chunkBuffer
                    )

                    channel.connection.writeFrame(frame)
                }
            } else {
                val frame = Frame(
                    FrameHeader(FrameType.Body, channel.id, body.size),
                    body
                )

                channel.connection.writeFrame(frame)
            }
        }

        channel.connection.flush()
    }
}
