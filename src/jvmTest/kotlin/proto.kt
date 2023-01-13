import dimensional.usagi.protocol.AMQP
import dimensional.usagi.protocol.Method
import dimensional.usagi.protocol.kxser.Amqp
import dimensional.usagi.protocol.kxser.annotations.AMQBit
import dimensional.usagi.protocol.kxser.decode
import dimensional.usagi.protocol.kxser.encode
import dimensional.usagi.protocol.reader.MethodProtocolReader
import dimensional.usagi.protocol.reader.amqp
import dimensional.usagi.protocol.type.FieldTable
import dimensional.usagi.protocol.type.LongString
import dimensional.usagi.protocol.writer.MethodProtocolWriter
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable

val ByteArray.methodReader: MethodProtocolReader get() = MethodProtocolReader(ByteReadPacket(this).amqp)

suspend fun main() {
    val old = AMQP.Connection.StartOk {
        clientProperties = emptyMap()
        response = LongString.Empty
    }.asFrame(1)

    /* */
    val new = StartOk(
        FieldTable(emptyMap()),
        "PLAIN",
        LongString.Empty,
        "en_US"
    ).asFrame(1)

    println(old.body.contentToString().encodeBase64())
    println(new.body.contentToString().encodeBase64())

    println(AMQP.readMethodFrom(old.body.methodReader))

    val reader = new.body.methodReader
    reader.readShortSigned()
    reader.readShortSigned()
    println(Amqp.decode<StartOk>(reader))

    require(new.body.contentEquals(old.body))
}

@Serializable
data class Open(
    val virtualHost: String,
    val capabilities: String,
    @AMQBit val insist: Boolean,
) : Method() {
    override fun classId(): Short = 10

    override fun methodId(): Short = 40

    override fun methodName(): String = "connection.open"

    override fun hasContent(): Boolean = false

    override suspend fun writeTo(writer: MethodProtocolWriter) {
        Amqp.encode(this, writer)
    }
}

@Serializable
data class StartOk(
    val clientProperties: FieldTable,
    val mechanism: String,
    val response: LongString,
    val locale: String,
) : Method() {
    override fun classId(): Short = 10

    override fun methodId(): Short = 11

    override fun methodName(): String = "connection.start-ok"

    public override fun hasContent(): Boolean = false

    public override suspend fun writeTo(writer: MethodProtocolWriter) {
        Amqp.encode(this, writer)
    }
}
