@file:Suppress("unused")

package codegen

import com.squareup.kotlinpoet.*
import kotlinx.serialization.json.*

enum class RequiredBehavior {
    NotNullDelegate,
    LateInit
}

object AMQP {
    interface Named {
        val name: String

        val normalizedName: String
            get() = name
                .split('-')
                .joinToString("") { it.capitalize() }
    }

    enum class Type(
        val internalType: TypeName,
        private val libraryName: String,
        val requiredBehavior: RequiredBehavior,
        val convert: String? = null,
        val exposedType: TypeName = internalType,
    ) {
        /** 64-bit unsigned long timestamp */
        TIMESTAMP(INSTANT, "Timestamp", RequiredBehavior.LateInit),

        /** byte */
        OCTET(INT, "Octet", RequiredBehavior.NotNullDelegate),

        /** boolean value */
        BIT(BOOLEAN, "Bit", RequiredBehavior.NotNullDelegate),

        /** k/v pair */
        TABLE(
            typeNameOf<Map<String, Any?>>(),
            "FieldTable",
            RequiredBehavior.LateInit,
        ),

        /**  */
        LONGSTR(LONG_STRING, "LongString", RequiredBehavior.NotNullDelegate, "LongString(%L)"),

        /** string sized between 0-255 bytes */
        SHORTSTR(STRING, "ShortString", RequiredBehavior.LateInit),

        /** 2-bit unsigned int */
        SHORT(com.squareup.kotlinpoet.SHORT, "ShortUnsigned", RequiredBehavior.NotNullDelegate),

        /** 4-bit unsigned int */
        LONG(INT, "LongUnsigned", RequiredBehavior.NotNullDelegate),

        /** 8-bit unsigned int */
        LONGLONG(com.squareup.kotlinpoet.LONG, "LongLongUnsigned", RequiredBehavior.NotNullDelegate),
        ;

        /** used to find this type within the codegen json */
        val id: String get() = name.toLowerCase()

        /** method within the ProtocolReader used to read this type. */
        val readerMethod: String
            get() = "read$libraryName"

        /** method within the ProtocolWriter used to write this type. */
        val writerMethod: String
            get() = "write$libraryName"

        lateinit var lol: String
    }

    data class Class(
        val id: Int,
        override val name: String,
        val methods: List<Method>,
        val properties: List<ClassProperty> = emptyList(),
    ) : Parent {
        companion object {
            fun fromJson(domains: Map<String, Type>, classData: JsonObject, methodIgnoreList: IntArray = IntArray(0)): Class = Class(
                classData["id"]!!.jsonPrimitive.int,
                classData["name"]!!.jsonPrimitive.content,
                classData["methods"]!!.jsonArray
                    .map { it.jsonObject }
                    .map { Method.fromJson(domains, it) },
                classData["properties"]?.jsonArray
                    ?.map { it.jsonObject }
                    ?.map(ClassProperty::fromJson)
                    ?: emptyList(),
            )
        }

        override val children: List<Named> get() = properties
    }

    interface Parent : Named {
        val children: List<Named>
    }

    data class ClassProperty(
        override val name: String,
        val type: Type,
    ) : Named {
        companion object {
            fun fromJson(propertyData: JsonObject): ClassProperty = ClassProperty(
                propertyData["name"]!!.jsonPrimitive.content,
                AMQP.Type.valueOf(propertyData["type"]!!.jsonPrimitive.content.toUpperCase())
            )
        }

        override val normalizedName: String
            get() = super.normalizedName.decapitalize()
    }

    data class Method(
        val id: Int,
        override val name: String,
        val arguments: List<MethodArgument> = emptyList(),
        val synchronous: Boolean = false,
        val content: Boolean = false,
    ) : Parent {
        companion object {
            fun fromJson(domains: Map<String, Type>, methodData: JsonObject): Method = Method(
                methodData["id"]!!.jsonPrimitive.int,
                methodData["name"]!!.jsonPrimitive.content,
                methodData["arguments"]!!.jsonArray
                    .map { it.jsonObject }
                    .map { MethodArgument.fromJson(domains, it) },
                methodData["synchronous"]?.jsonPrimitive?.boolean ?: false,
                methodData["content"]?.jsonPrimitive?.boolean ?: false,
            )
        }

        override val children: List<Named> get() = arguments
    }

    data class MethodArgument(
        override val name: String,
        val type: Type,
        val defaultValue: String? = null,
    ) : Named {
        companion object {
            fun fromJson(domains: Map<String, Type>, argumentData: JsonObject): MethodArgument {
                val type: Type = if ("domain" in argumentData) {
                    val domain = argumentData["domain"]!!.jsonPrimitive.content
                    domains[domain] ?: error("Missing domain: $domain")
                } else {
                    AMQP.Type.valueOf(argumentData["type"]!!.jsonPrimitive.content.toUpperCase())
                }

                val defaultValue: String? = when (val value = argumentData["default-value"]) {
                    is JsonObject -> "mapOf(${value.entries.joinToString(", ") { "\"${it.key}\" to ${it.value}" }})"
                    is JsonArray -> value.joinToString(", ", prefix = "listOf(", postfix = ")")
                    null -> null
                    else -> "$value"
                }

                return MethodArgument(
                    argumentData["name"]!!.jsonPrimitive.content,
                    type,
                    defaultValue,
                )
            }
        }

        override val normalizedName: String
            get() = super.normalizedName.decapitalize()
    }
}
