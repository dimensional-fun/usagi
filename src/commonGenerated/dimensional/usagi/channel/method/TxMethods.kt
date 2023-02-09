package dimensional.usagi.channel.method

import dimensional.usagi.channel.Channel
import dimensional.usagi.protocol.AMQP
import kotlin.jvm.JvmInline

@JvmInline
public value class TxMethods(
  public val channel: Channel,
) {
  public suspend fun select(): AMQP.Tx.SelectOk {
    val method = AMQP.Tx.Select
    val ok = channel.rpc(method)
    require(ok.method is AMQP.Tx.SelectOk) { 
      "Expected 'tx.select-ok', not ${ok.method.methodName()}"
    }

    return ok.method
  }

  public suspend fun commit(): AMQP.Tx.CommitOk {
    val method = AMQP.Tx.Commit
    val ok = channel.rpc(method)
    require(ok.method is AMQP.Tx.CommitOk) { 
      "Expected 'tx.commit-ok', not ${ok.method.methodName()}"
    }

    return ok.method
  }

  public suspend fun rollback(): AMQP.Tx.RollbackOk {
    val method = AMQP.Tx.Rollback
    val ok = channel.rpc(method)
    require(ok.method is AMQP.Tx.RollbackOk) { 
      "Expected 'tx.rollback-ok', not ${ok.method.methodName()}"
    }

    return ok.method
  }
}

public val Channel.tx: TxMethods
  get() = TxMethods(this)
