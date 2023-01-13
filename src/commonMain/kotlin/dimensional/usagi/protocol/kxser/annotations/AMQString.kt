package dimensional.usagi.protocol.kxser.annotations

import dimensional.usagi.protocol.kxser.AMQStringType
import kotlinx.serialization.SerialInfo

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class AMQString(val type: AMQStringType)
