package mixtape.oss.usagi.channel.consumer

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.Channel as CoroutineChannel
import mixtape.oss.usagi.channel.Channel
import mixtape.oss.usagi.channel.event.ConsumerEvent
import mixtape.oss.usagi.channel.method.basic
import mu.KLogger
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext

public data class Consumer(
    val channel: Channel,
    val tag: String,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        channel.scope.coroutineContext + SupervisorJob() + CoroutineName("Consumer[$tag]")

    private val eventFlow = channel.events
        .filterIsInstance<ConsumerEvent>()
        .filter { it.consumer.tag == tag }
        .shareIn(this, SharingStarted.Eagerly)

    public val events: SharedFlow<ConsumerEvent>
        get() = eventFlow

    public suspend fun cancel(noWait: Boolean = false) {
        channel.basic.cancel { consumerTag = tag; nowait(noWait) }
    }
}

@PublishedApi
internal val consumerOnLog: KLogger = KotlinLogging.logger("mixtape.oss.usagi.channel.consumer.Consumer#on")

public inline fun <reified T : ConsumerEvent> Consumer.on(
    scope: CoroutineScope = this,
    noinline block: suspend T.() -> Unit
): Job {
    return events
        .buffer(CoroutineChannel.UNLIMITED)
        .filterIsInstance<T>()
        .onEach { scope.launch { runCatching { block(it) }.onFailure { consumerOnLog.catching(it) } } }
        .launchIn(scope)
}
