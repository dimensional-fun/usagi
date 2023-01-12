package codegen

import com.squareup.kotlinpoet.ClassName

val protocolPackage = project.rootPackage + ".protocol"
val channelPackage = project.rootPackage + ".channel"

//val INDENT = "   "
val INDENT = "  "

val DELEGATES = ClassName("kotlin.properties", "Delegates")

val METHOD = ClassName(protocolPackage, "Method")

val CONTENT_HEADER = ClassName("$channelPackage.command", "ContentHeader")
val CONTENT_HEADER_PROPERTIES = CONTENT_HEADER.nestedClass("Properties")

val PROTOCOL_PROPERTIES_WRITER = ClassName("$protocolPackage.writer", "ProtocolPropertiesWriter")
val METHOD_PROTOCOL_WRITER = ClassName("$protocolPackage.writer", "MethodProtocolWriter")

val PROTOCOL_PROPERTIES_READER = ClassName("$protocolPackage.reader", "ProtocolPropertiesReader")
val METHOD_PROTOCOL_READER = ClassName("$protocolPackage.reader", "MethodProtocolReader")
