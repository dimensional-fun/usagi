package mixtape.oss.usagi.channel.method

import kotlinx.coroutines.sync.withLock
import mixtape.oss.usagi.channel.Channel
import mixtape.oss.usagi.protocol.AMQP
import kotlin.jvm.JvmInline

public val Channel.exchange: ExchangeMethods get() = ExchangeMethods(this)

@JvmInline
public value class ExchangeMethods(private val channel: Channel) {
    /**
     * @param method
     */
    public suspend fun declare(method: AMQP.Exchange.Declare): AMQP.Exchange.DeclareOk /*= channel.mutex.withLock */{
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Exchange.DeclareOk) { "Expected `exchange.declare-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun declare(block: AMQP.Exchange.Declare.Builder.() -> Unit): AMQP.Exchange.DeclareOk {
        return declare(AMQP.Exchange.Declare(block))
    }

    /**
     * @param method
     */
    public suspend fun delete(method: AMQP.Exchange.Delete): AMQP.Exchange.DeleteOk /*= channel.mutex.withLock */{
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Exchange.DeleteOk) { "Expected `exchange.delete-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun delete(block: AMQP.Exchange.Delete.Builder.() -> Unit): AMQP.Exchange.DeleteOk {
        return delete(AMQP.Exchange.Delete(block))
    }

    /**
     * @param method
     */
    public suspend fun bind(method: AMQP.Exchange.Bind): AMQP.Exchange.BindOk /*= channel.mutex.withLock */{
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Exchange.BindOk) { "Expected `exchange.bind-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun bind(block: AMQP.Exchange.Bind.Builder.() -> Unit): AMQP.Exchange.BindOk {
        return bind(AMQP.Exchange.Bind(block))
    }

    /**
     * @param method
     */
    public suspend fun unbind(method: AMQP.Exchange.Unbind): AMQP.Exchange.UnbindOk /*= channel.mutex.withLock */{
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Exchange.UnbindOk) { "Expected `exchange.unbind-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun unbind(block: AMQP.Exchange.Unbind.Builder.() -> Unit): AMQP.Exchange.UnbindOk {
        return unbind(AMQP.Exchange.Unbind(block))
    }
}
