package mixtape.oss.usagi.channel.command

import io.ktor.utils.io.core.*
import mixtape.oss.usagi.protocol.Method
import mixtape.oss.usagi.connection.frame.Frame
import mixtape.oss.usagi.connection.frame.FrameType
import mixtape.oss.usagi.protocol.reader.amqp

public class CommandAssembler {
    private var state: State = State.ExpectingMethod

    private var method: Method? = null
    private var header: ContentHeader? = null
    private var body: ContentBody.Fragmented? = null

    /**
     * Whether this assembler has completed a command.
     */
    public val hasCompleted: Boolean get() = state == State.Completed

    /**
     * Assembles a new command if [state] is [State.Completed], throws otherwise.
     * If this assembler is being re-used, make sure to call [reset]
     *
     * @return the assembled [Command]
     */
    public fun assemble(): Command {
        require(hasCompleted) { "Assembler is not in completed state." }
        return Command(
            method!!,
            header,
            body
        )
    }

    /**
     * Resets this assembler.
     */
    public fun reset(): CommandAssembler {
        state = State.ExpectingMethod
        method = null
        header = null
        body = null
        return this
    }

    /**
     *
     */
    public suspend fun handleFrame(frame: Frame): Boolean {
        when (state) {
            State.ExpectingMethod -> handleMethod(frame)
            State.ExpectingContentHeader -> handleContentHeader(frame)
            State.ExpectingContentBody -> handleContentBody(frame)
            else -> error("Invalid state: $state")
        }

        return hasCompleted
    }

    private suspend fun handleMethod(frame: Frame) {
        require(frame.header.type is FrameType.Method) {
            "Expected FrameType.Method, not ${frame.header.type}"
        }

        method = Method.fromFrame(frame)
        state = if (method?.hasContent() == true) State.ExpectingContentHeader else State.Completed
    }

    private suspend fun handleContentHeader(frame: Frame) {
        require(frame.header.type is FrameType.Header) {
            "Expected FrameType.Header, not ${frame.header.type}"
        }

        val payload = ByteReadPacket(frame.body)

        header = ContentHeader(payload.amqp)
        body = ContentBody.Fragmented(header!!.bodySize)

        state = State.ExpectingContentBody
    }

    private fun handleContentBody(frame: Frame) {
        require(frame.header.type is FrameType.Body) {
            "Expected FrameType.Body, not ${frame.header.type}"
        }

        if (body?.handle(frame) == true) {
            state = State.Completed
        }
    }

    public enum class State {
        ExpectingMethod,
        ExpectingContentHeader,
        ExpectingContentBody,
        Completed
        ;
    }
}
