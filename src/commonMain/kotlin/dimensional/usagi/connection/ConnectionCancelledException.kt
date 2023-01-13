package dimensional.usagi.connection

import dimensional.usagi.protocol.AMQP
import kotlinx.coroutines.CancellationException

public class ConnectionCancelledException(
    reason: AMQP.Connection.Close,
    override val cause: Throwable? = null,
    public val manual: Boolean,
) : CancellationException("${reason.replyCode} ${reason.replyText} (C${reason.classId} M${reason.methodId})")
