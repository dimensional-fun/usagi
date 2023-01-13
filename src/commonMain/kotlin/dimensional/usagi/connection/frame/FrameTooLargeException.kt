package dimensional.usagi.connection.frame

public class FrameTooLargeException(public val frame: Frame, public val maxSize: Int) : Exception(
    "Frame exceeds max size of $maxSize: ${frame.size}"
)
