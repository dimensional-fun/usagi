package mixtape.oss.usagi.protocol.kxser.annotations

import kotlinx.serialization.SerialInfo
import mixtape.oss.usagi.protocol.kxser.AMQStringType

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class AMQString(val type: AMQStringType)
