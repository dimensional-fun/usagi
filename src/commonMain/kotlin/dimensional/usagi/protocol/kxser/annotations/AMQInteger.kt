package dimensional.usagi.protocol.kxser.annotations

import dimensional.usagi.protocol.kxser.AMQIntegerType
import kotlinx.serialization.SerialInfo

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class AMQInteger(val type: AMQIntegerType, val signed: Boolean = false)
