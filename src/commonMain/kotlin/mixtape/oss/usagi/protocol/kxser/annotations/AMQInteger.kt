package mixtape.oss.usagi.protocol.kxser.annotations

import kotlinx.serialization.SerialInfo
import mixtape.oss.usagi.protocol.kxser.AMQIntegerType

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class AMQInteger(val type: AMQIntegerType, val signed: Boolean = false)
