package mixtape.oss.usagi.channel.method

import mixtape.oss.usagi.channel.Channel
import mixtape.oss.usagi.channel.command.Command
import mixtape.oss.usagi.channel.command.ContentBody
import mixtape.oss.usagi.channel.command.ContentHeader
import mixtape.oss.usagi.channel.consumer.Consumer
import mixtape.oss.usagi.protocol.AMQP
import kotlin.jvm.JvmInline

public val Channel.basic: BasicMethods get() = BasicMethods(this)

@JvmInline
public value class BasicMethods(private val channel: Channel) {
    /**
     * @param method
     */
    public suspend fun qos(method: AMQP.Basic.Qos): AMQP.Basic.QosOk {
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Basic.QosOk) { "Expected `basic.qos-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun qos(block: AMQP.Basic.Qos.Builder.() -> Unit): AMQP.Basic.QosOk {
        return qos(AMQP.Basic.Qos(block))
    }

    /**
     * @param method
     */
    public suspend fun consume(method: AMQP.Basic.Consume): Consumer {
        return channel.createConsumer(method)
    }

    public suspend fun consume(
        block: AMQP.Basic.Consume.Builder.() -> Unit
    ): Consumer {
        return consume(AMQP.Basic.Consume(block))
    }

    /**
     * @param method
     */
    public suspend fun cancel(method: AMQP.Basic.Cancel): AMQP.Basic.CancelOk {
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Basic.CancelOk) { "Expected `basic.cancel-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun cancel(block: AMQP.Basic.Cancel.Builder.() -> Unit): AMQP.Basic.CancelOk {
        return cancel(AMQP.Basic.Cancel(block))
    }

    /**
     * @param method
     */
    public suspend fun publish(
        block: Publish.() -> Unit,
    ) {
        val command = Publish()
            .apply(block)
            .build()

        channel.send(command)
    }

    /**
     * @param method
     */
    public suspend fun get(method: AMQP.Basic.Get): AMQP.Basic.GetOk {
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Basic.GetOk) { "Expected `basic.get-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun get(block: AMQP.Basic.Get.Builder.() -> Unit): AMQP.Basic.GetOk {
        return get(AMQP.Basic.Get(block))
    }

    /**
     * @param method
     */
    public suspend fun ack(method: AMQP.Basic.Ack) {
        channel.send(method)
    }

    public suspend fun ack(block: AMQP.Basic.Ack.Builder.() -> Unit) {
        return ack(AMQP.Basic.Ack(block))
    }

    /**
     * @param method
     */
    public suspend fun recover(method: AMQP.Basic.Recover): AMQP.Basic.RecoverOk {
        val ok = channel.rpc(method)
        require(ok.method is AMQP.Basic.RecoverOk) { "Expected `basic.recover-ok`, not ${ok.method.methodName()}" }
        return ok.method
    }

    public suspend fun recover(block: AMQP.Basic.Recover.Builder.() -> Unit): AMQP.Basic.RecoverOk {
        return recover(AMQP.Basic.Recover(block))
    }

    /**
     * @param method
     */
    public suspend fun nack(method: AMQP.Basic.Nack) {
        channel.send(method)
    }

    public suspend fun nack(block: AMQP.Basic.Nack.Builder.() -> Unit) {
        return nack(AMQP.Basic.Nack(block))
    }

    public class Publish {
        private var properties: AMQP.Basic.Properties.Builder = AMQP.Basic.Properties.Builder()

        private var options: AMQP.Basic.Publish.Builder = AMQP.Basic.Publish.Builder()

        public lateinit var data: ByteArray

        public fun properties(block: AMQP.Basic.Properties.Builder.() -> Unit): Publish {
            properties.block()
            return this
        }

        public fun options(block: AMQP.Basic.Publish.Builder.() -> Unit): Publish {
            options.block()
            return this
        }

        public fun build(): Command {
            val header = ContentHeader(
                data.size.toLong(),
                properties.build()
            )

            return Command(options.build(), header, ContentBody.Whole(data))
        }
    }
}
