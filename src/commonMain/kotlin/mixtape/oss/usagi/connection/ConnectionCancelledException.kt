package mixtape.oss.usagi.connection

import kotlinx.coroutines.CancellationException
import mixtape.oss.usagi.protocol.AMQP

public class ConnectionCancelledException(
    reason: AMQP.Connection.Close,
    override val cause: Throwable? = null,
    public val manual: Boolean,
) : CancellationException("${reason.replyCode} ${reason.replyText} (C${reason.classId} M${reason.methodId})")
