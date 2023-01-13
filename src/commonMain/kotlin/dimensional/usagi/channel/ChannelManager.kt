package dimensional.usagi.channel

import dimensional.usagi.connection.Connection
import dimensional.usagi.tools.IntAllocator
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import kotlin.time.Duration.Companion.seconds

public class ChannelManager(private val connection: Connection) {
    public companion object {
        private val log = KotlinLogging.logger {  }
    }

    private val channelStore = mutableMapOf<Int, Channel>()
    private val channelIds = IntAllocator(1, connection.preferences.maxChannelCount)
    private val mutex = Mutex()

    /**
     * Shuts down all allocated channels.
     */
    public suspend fun shutdown() {
        val channels = mutex.withLock { channelStore.values }
        for (channel in channels) {
            free(channel)

            try {
                withTimeout(12.seconds) { channel.shutdown() }
            } catch (ex: TimeoutCancellationException) {
                log.warn(ex) { "[Channel ${channel.id}] Did not shut down in time." }
            } catch (ex: Exception) {
                log.warn(ex) { "[Channel ${channel.id}] Did not shut down property:" }
            }
        }
    }

    /**
     * Attempts to retrieve a channel with the specified [id].
     *
     * @param id The ID of the channel to retrieve.
     * @return The channel with [id], or null if it doesn't exist.
     */
    public suspend fun get(id: Int): Channel? = mutex.withLock {
        channelStore[id]
    }

    /**
     * Creates and allocates a new channel to the underlying connection.
     *
     * @return The allocated [Channel], or null if a channel couldn't be allocated.
     */
    public suspend fun create(): Channel? {
        val channel = mutex.withLock {
            val id = channelIds.allocate()
            if (id == -1) {
                /* cannot allocate any more channels. */
                return null
            }

            createChannel(id)
        }

        channel.open()
        return channel
    }

    /**
     * Creates and reserves a channel with the specified ID.
     *
     * @param id The ID of the channel to allocate.
     * @return The allocated [Channel], or null if [id] could not be reserved.
     */
    public suspend fun create(id: Int): Channel? {
        val channel = mutex.withLock {
            if (!channelIds.reserve(id)) {
                /* unable to reserve the provided id */
                return null
            }

            createChannel(id)
        }

        channel.open()
        return channel
    }

    /**
     * Attempts to remove the provided channel from the internal store and
     * free its ID for allocation.
     *
     * @param channel The channel to free.
     * @return `true` if the channel was freed, `false` otherwise.
     */
    public suspend fun free(channel: Channel): Boolean = mutex.withLock {
        if (channel.id !in channelStore) {
            /* nothing for us to do... */
            return false
        }

        if (channelStore[channel.id] != channel) {
            /* not one of ours... */
            return false
        }

        channelStore.remove(channel.id)
        channelIds.free(channel.id)
        return true
    }

    private fun createChannel(id: Int): Channel {
        /* create a new channel and store it. */
        val channel = Channel(connection, id)
        channelStore[id] = channel

        /* open the channel and return it */
        return channel
    }
}
