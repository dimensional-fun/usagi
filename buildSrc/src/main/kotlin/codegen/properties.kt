package codegen

import com.squareup.kotlinpoet.*

fun TypeSpec.Builder.amqpReadPropertiesFromFunction(amqpClasses: List<AMQP.Class>): TypeSpec.Builder {
    val spec = FunSpec.builder("readPropertiesFrom")
        .addModifiers(KModifier.PUBLIC, KModifier.SUSPEND)
        .addParameter("classId", SHORT)
        .addParameter("reader", PROTOCOL_PROPERTIES_READER)
        .returns(CONTENT_HEADER_PROPERTIES)

    spec.addCode(
        """
        |return when (classId.toInt()) {
        |""".trimMargin()
    )

    for (amqpClass in amqpClasses) {
        if (amqpClass.properties.isEmpty()) {
            continue
        }

        spec.addCode("$INDENT${amqpClass.id} -> ${amqpClass.normalizedName}.Properties(reader)\n")
    }

    spec.addCode("""${INDENT}else -> error("Invalid class id: ${"$"}classId")""")
    spec.addCode("\n}")

    return addFunction(spec.build())
}

fun TypeSpec.Builder.amqpClassProperties(amqpClass: AMQP.Class): TypeSpec.Builder {
    val spec = TypeSpec.classBuilder("Properties")
        .addSuperinterface(CONTENT_HEADER_PROPERTIES)
        .addModifiers(KModifier.PUBLIC, KModifier.DATA)
        .amqpClassPropertiesPrimaryConstructor(amqpClass)
        .amqpClassPropertiesCompanion(amqpClass)
        .amqpClassPropertiesBuilder(amqpClass)
        .amqpClassPropertiesWriter(amqpClass)
        .amqpToBuilderFunction(amqpClass)

    spec.addFunction(
        FunSpec.builder("classId")
            .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
            .addCode("return %L", amqpClass.id)
            .returns(SHORT)
            .build()
    )

    return addType(spec.build())
}

fun TypeSpec.Builder.amqpClassPropertiesCompanion(amqpClass: AMQP.Class): TypeSpec.Builder {
    val spec = TypeSpec.companionObjectBuilder()
        .addModifiers(KModifier.PUBLIC)
        .addFunction(amqpClassPropertiesCompanionReader(amqpClass))
        .addFunction(amqpCompanionBuilderFunction(ClassName("", "Properties")))
        .build()

    return addType(spec)
}

fun amqpClassPropertiesCompanionReader(amqpClass: AMQP.Class): FunSpec {
    val invoke = FunSpec.builder("invoke")
        .addModifiers(KModifier.PUBLIC, KModifier.SUSPEND, KModifier.OPERATOR)
        .addParameter("reader", PROTOCOL_PROPERTIES_READER)
        .returns(ClassName("", "Properties"))

    invoke.addCode(
        amqpClass.properties.joinToString("\n") {
            "val ${it.normalizedName}Present = reader.readPresence()"
        }
    )
    invoke.addCode(
        """|
           |reader.finishPresence()
           |return Properties(
           |""".trimMargin()
    )

    invoke.addCode(
        amqpClass.properties.joinToString(",\n") {
            "${INDENT}if (${it.normalizedName}Present) reader.${it.type.readerMethod}() else null"
        }
    )

    invoke.addCode("\n)")
    return invoke.build()
}

fun TypeSpec.Builder.amqpClassPropertiesWriter(amqpClass: AMQP.Class): TypeSpec.Builder {
    val writeTo = FunSpec.builder("writeTo")
        .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE, KModifier.SUSPEND)
        .addParameter("writer", PROTOCOL_PROPERTIES_WRITER)

    writeTo.addCode(
        amqpClass.properties.joinToString("\n") {
            "writer.writePresence(${it.normalizedName} != null)"
        }
    )
    writeTo.addCode(
        """
    |
    |writer.finishPresence()
    |
    """.trimMargin()
    )

    writeTo.addCode(
        amqpClass.properties.joinToString("\n") {
            "${it.normalizedName}?.let { writer.${it.type.writerMethod}(it) }"
        }
    )

    writeTo.addCode("\n")
    return addFunction(writeTo.build())
}

fun TypeSpec.Builder.amqpClassPropertiesBuilder(amqpClass: AMQP.Class): TypeSpec.Builder {
    val spec = TypeSpec.classBuilder("Builder")
        .addModifiers(KModifier.PUBLIC)

    val builder = FunSpec.builder("build")
        .returns(ClassName("", "Properties"))
        .addCode("return Properties(")

    // Simplified version of the builder for methods
    for (prop in amqpClass.properties) {
        spec.addProperty(
            PropertySpec
                .builder(prop.normalizedName, prop.type.exposedType.copy(true))
                .mutable()
                .initializer("null")
                .build()
        )

        /* useful function for chaining */
        spec.addFunction(
            FunSpec.builder(prop.normalizedName)
                //.addKdoc("Configures [${prop.normalizedName}] with the specified value")
                .addModifiers(KModifier.PUBLIC)
                .returns(ClassName("", "Builder"), /*CodeBlock.of("[Builder], useful for chaining.")*/)
                .addParameter(
                    ParameterSpec.builder("value", prop.type.exposedType.copy(nullable = true))
                    //.addKdoc("The new value.")
                    .build())
                .addCode(
                    """
                    |${prop.normalizedName} = value
                    |return this
                    """.trimMargin()
                )
                .build()
        )

        /* add parameter to constructor call in builder. */
        builder.addCode("%L", prop.normalizedName)
        prop.type.convert
            ?.takeIf { prop.type.internalType != prop.type.exposedType }
            ?.let { builder.addCode("?.let { %L }", CodeBlock.of(it, "it")) }

        builder.addCode(", ")
    }

    spec.addFunction(
        builder
            .addCode(")")
            .build()
    )

    return addType(spec.build())
}

fun TypeSpec.Builder.amqpClassPropertiesPrimaryConstructor(amqpClass: AMQP.Class): TypeSpec.Builder {
    val spec = FunSpec.constructorBuilder()
    for (property in amqpClass.properties) {
        val type = property.type.internalType.copy(true)
        spec.addParameter(property.normalizedName, type)

        addProperty(
            PropertySpec.builder(property.normalizedName, type)
                .addModifiers(KModifier.PUBLIC)
                .initializer(property.normalizedName)
                .build()
        )
    }

    return primaryConstructor(spec.build())
}
