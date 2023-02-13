package dimensional.usagi.channel

import dimensional.usagi.channel.event.ChannelEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import mu.KLogger
import mu.KotlinLogging
import kotlinx.coroutines.channels.Channel as CoroutineChannel

public val onLog: KLogger = KotlinLogging.logger("dimensional.usagi.channel.Channel")

public inline fun <reified E : ChannelEvent> Channel.on(
    scope: CoroutineScope = this.scope,
    noinline block: suspend E.() -> Unit
): Job = events
    .buffer(CoroutineChannel.UNLIMITED)
    .filterIsInstance<E>()
    .onEach { runCatching { block(it) }.onFailure { onLog.catching(it) } }
    .launchIn(scope)
