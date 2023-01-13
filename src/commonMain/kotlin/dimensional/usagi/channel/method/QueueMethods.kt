package dimensional.usagi.channel.method

import dimensional.usagi.channel.Channel
import dimensional.usagi.protocol.AMQP
import kotlin.jvm.JvmInline

public val Channel.queue: QueueMethods get() = QueueMethods(this)

@JvmInline
public value class QueueMethods(public val channel: Channel) {
    /**
     * @param method
     */
    public suspend fun declare(method: AMQP.Queue.Declare): AMQP.Queue.DeclareOk /*= channel.mutex.withLock */{
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Queue.DeclareOk) { "Expected `queue.declare-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun declare(block: AMQP.Queue.Declare.Builder.() -> Unit = {}): AMQP.Queue.DeclareOk {
        return declare(AMQP.Queue.Declare(block))
    }

    /**
     * @param method
     */
    public suspend fun bind(method: AMQP.Queue.Bind): AMQP.Queue.BindOk /*= channel.mutex.withLock */{
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Queue.BindOk) { "Expected `queue.bind-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun bind(build: AMQP.Queue.Bind.Builder.() -> Unit): AMQP.Queue.BindOk {
        return bind(AMQP.Queue.Bind(build))
    }

    /**
     * @param method
     */
    public suspend fun purge(method: AMQP.Queue.Purge): AMQP.Queue.PurgeOk /*= channel.mutex.withLock */{
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Queue.PurgeOk) { "Expected `queue.purge-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun purge(block: AMQP.Queue.Purge.Builder.() -> Unit): AMQP.Queue.PurgeOk {
        return purge(AMQP.Queue.Purge(block))
    }

    /**
     * @param method
     */
    public suspend fun delete(method: AMQP.Queue.Delete): AMQP.Queue.DeleteOk /*= channel.mutex.withLock */{
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Queue.DeleteOk) { "Expected `queue.delete-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun delete(build: AMQP.Queue.Delete.Builder.() -> Unit): AMQP.Queue.DeleteOk {
        return delete(AMQP.Queue.Delete(build))
    }

    /**
     * @param method
     */
    public suspend fun unbind(method: AMQP.Queue.Unbind): AMQP.Queue.UnbindOk /*= channel.mutex.withLock */{
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Queue.UnbindOk) { "Expected `queue.unbind-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun unbind(build: AMQP.Queue.Unbind.Builder.() -> Unit): AMQP.Queue.UnbindOk {
        return unbind(AMQP.Queue.Unbind(build))
    }
}
