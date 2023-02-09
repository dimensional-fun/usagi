package dimensional.usagi.channel.method

import dimensional.usagi.channel.Channel
import dimensional.usagi.protocol.AMQP
import kotlin.Unit
import kotlin.jvm.JvmInline

@JvmInline
public value class ChannelMethods(
  public val channel: Channel,
) {
  public suspend fun `open`(block: AMQP.Channel.Open.Builder.() -> Unit): AMQP.Channel.OpenOk =
      open(AMQP.Channel.Open.Builder().apply(block).build())

  public suspend fun `open`(method: AMQP.Channel.Open): AMQP.Channel.OpenOk {
    val ok = channel.rpc(method)
    require(ok.method is AMQP.Channel.OpenOk) { 
      "Expected 'channel.open-ok', not ${ok.method.methodName()}"
    }

    return ok.method
  }

  public suspend fun flow(block: AMQP.Channel.Flow.Builder.() -> Unit): AMQP.Channel.FlowOk =
      flow(AMQP.Channel.Flow.Builder().apply(block).build())

  public suspend fun flow(method: AMQP.Channel.Flow): AMQP.Channel.FlowOk {
    val ok = channel.rpc(method)
    require(ok.method is AMQP.Channel.FlowOk) { 
      "Expected 'channel.flow-ok', not ${ok.method.methodName()}"
    }

    return ok.method
  }

  public suspend fun close(block: AMQP.Channel.Close.Builder.() -> Unit): AMQP.Channel.CloseOk =
      close(AMQP.Channel.Close.Builder().apply(block).build())

  public suspend fun close(method: AMQP.Channel.Close): AMQP.Channel.CloseOk {
    val ok = channel.rpc(method)
    require(ok.method is AMQP.Channel.CloseOk) { 
      "Expected 'channel.close-ok', not ${ok.method.methodName()}"
    }

    return ok.method
  }
}

public val Channel.channel: ChannelMethods
  get() = ChannelMethods(this)
