package dimensional.usagi.channel.consumer

import dimensional.usagi.annotations.InternalUsagiAPI
import dimensional.usagi.channel.Channel
import dimensional.usagi.channel.method.basic
import dimensional.usagi.protocol.AMQP
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KLogger
import mu.KotlinLogging
import kotlinx.coroutines.channels.Channel as CoroutineChannel

public data class Consumer(
    val channel: Channel,
    val tag: String,
) {
    public companion object {
        public val COMPLETED_DEFERRED: Deferred<Unit> = CompletableDeferred<Unit>().apply { complete(Unit) }
    }

    val scope: CoroutineScope = CoroutineScope(
        channel.scope.coroutineContext + SupervisorJob() + CoroutineName("Consumer[$tag]")
    )

    private val waiters = mutableListOf<CompletableDeferred<Unit>>()
    private val deliveryFlow = MutableSharedFlow<Delivery>(extraBufferCapacity = Int.MAX_VALUE)

    /**  */
    public val deliveries: SharedFlow<Delivery> get() = deliveryFlow

    public fun waitAsync(): Deferred<Unit> {
        if (!scope.isActive) return COMPLETED_DEFERRED
        val def = CompletableDeferred<Unit>()
        waiters += def
        return def
    }

    /**
     * Suspends until this consumer has been cancelled.
     */
    public suspend fun wait() {
        if (scope.isActive) waitAsync().await()
    }

    /**
     * Cancel this consumer.
     *
     * @param noWait If `true`, the server will not send a `basic.cancel-ok` frame.
     */
    public suspend fun cancel(noWait: Boolean = false) {
        channel.basic.cancel {
            consumerTag = tag
            nowait = noWait
        }
    }

    internal suspend fun handle(delivery: Delivery) {
        deliveryFlow.emit(delivery)
    }

    internal fun handle(method: AMQP.Basic.Cancel) {
        cancelled(true)
    }

    internal fun handle(method: AMQP.Basic.CancelOk) {
        cancelled(false)
    }

    @OptIn(InternalUsagiAPI::class)
    private fun cancelled(remote: Boolean) {
        waiters.forEach { it.complete(Unit) }
        scope.cancel(CancellationException(if (remote) "Consumer cancelled by server." else "Consumer has been cancelled."))
        channel.removeConsumer(tag)
    }
}

@PublishedApi
internal val consumerOnLog: KLogger = KotlinLogging.logger("dimensional.usagi.Consumer#on")

public inline fun Consumer.forEach(
    scope: CoroutineScope = this.scope,
    noinline block: suspend (Delivery) -> Unit
): Job = deliveries
    .buffer(CoroutineChannel.UNLIMITED)
    .onEach { runCatching { block(it) }.onFailure { consumerOnLog.catching(it) } }
    .launchIn(scope)
