package mixtape.oss.usagi.channel.consumer

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.Channel as CoroutineChannel
import mixtape.oss.usagi.channel.Channel
import mixtape.oss.usagi.channel.command.Command
import mixtape.oss.usagi.channel.consumer.event.ConsumerEvent
import mixtape.oss.usagi.channel.consumer.event.MessagePublishedEvent
import mixtape.oss.usagi.channel.method.basic
import mixtape.oss.usagi.protocol.AMQP
import mixtape.oss.usagi.tools.into
import mu.KLogger
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext

public data class Consumer(
    val channel: Channel,
    val tag: String,
) : CoroutineScope {
    private val eventFlow = MutableSharedFlow<ConsumerEvent>(extraBufferCapacity = Int.MAX_VALUE)

    public val events: SharedFlow<ConsumerEvent>
        get() = eventFlow

    override val coroutineContext: CoroutineContext =
        channel.scope.coroutineContext + SupervisorJob() + CoroutineName("Consumer[$tag]")

    public suspend fun cancel(noWait: Boolean = false) {
        channel.basic.cancel { consumerTag = tag; nowait(noWait) }
    }

    internal suspend fun handle(method: AMQP.Basic.Deliver, command: Command) {
        val delivery = Delivery(
            this,
            Delivery.Envelope(
                method.deliveryTag,
                method.redelivered,
                method.exchange,
                method.routingKey,
            ),
            command.header?.properties.into(),
            command.body!!.asBytes()
        )

        eventFlow.emit(MessagePublishedEvent(this, delivery))
    }

    internal suspend fun handle(method: AMQP.Basic.ConsumeOk, command: Command) {
        // TODO: consumer cancelled event
    }

    internal suspend fun handle(method: AMQP.Basic.RecoverOk, command: Command) {
        // TODO: consumer cancelled event
    }

    internal suspend fun handle(method: AMQP.Basic.Cancel, command: Command) {
        // TODO: consumer cancelled event
    }

    internal suspend fun handle(method: AMQP.Basic.CancelOk, command: Command) {
        // TODO: consumer cancelled by [Cancel] event
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
