package codegen

import com.squareup.kotlinpoet.*

fun generateMethodClass(
    amqpClass: AMQP.Class,
    amqpMethod: AMQP.Method,
): TypeSpec {
    val specBuilder = TypeSpec.classBuilder(amqpMethod.normalizedName)
        .superclass(METHOD)
        .addModifiers(KModifier.PUBLIC)
        //.addKdoc("Represents the AMQP method `${amqpClass.name}.${amqpMethod.name}` (${amqpMethod.id})")

    if (amqpMethod.arguments.isNotEmpty()) {
        specBuilder
            .addModifiers(KModifier.DATA)
            .amqpMethodPrimaryConstructor(amqpMethod)
    }

    return specBuilder
        .amqpMethodCompanionObject(amqpMethod)
        .overrideMethod("classId", SHORT) { addCode("return %L",amqpClass.id) }
        .overrideMethod("methodId", SHORT) { addCode("return %L", amqpMethod.id) }
        .overrideMethod("methodName", STRING) { addCode("return %S", "${amqpClass.name}.${amqpMethod.name}") }
        .overrideMethod("hasContent", BOOLEAN) { addCode("return %L", amqpMethod.content) }
        .addFunction(
            FunSpec.builder("writeTo")
                .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                .addParameter("writer", METHOD_PROTOCOL_WRITER)
                .amqpWriteArguments(amqpMethod)
                .build()
        )
        .amqpMethodBuilderClass(amqpMethod)
        .amqpToBuilderFunction(amqpMethod)
        .build()
}

fun TypeSpec.Builder.amqpMethodBuilderClass(amqpMethod: AMQP.Method): TypeSpec.Builder {
    val spec = TypeSpec.classBuilder("Builder")
        //.addKdoc("Convenience class for constructing an instance of [${amqpMethod.normalizedName}]")
        .addModifiers(KModifier.PUBLIC)

    val builder = FunSpec.builder("build")
        .returns(ClassName("", amqpMethod.normalizedName))
        .addCode("return ${amqpMethod.normalizedName}(")

    for (argument in amqpMethod.arguments) {
        val type = argument.type.exposedType

        /* create mutable property */
        val property = PropertySpec
            .builder(argument.normalizedName, type)
            .mutable()

        if (argument.defaultValue == null) {
            if (argument.type.requiredBehavior == RequiredBehavior.LateInit) {
                property.modifiers += KModifier.LATEINIT
            } else {
                property.delegate("%L.notNull()", DELEGATES)
            }
        } else {
            property.initializer(argument.type.convert ?: "%L", argument.defaultValue)
        }

        spec.addProperty(property.build())

        /* useful function for chaining */
        val function = FunSpec.builder(argument.normalizedName)
            //.addKdoc("Configures [${argument.normalizedName}] with the specified value")
            .addModifiers(KModifier.PUBLIC)
            .returns(ClassName("", "Builder"), /*CodeBlock.of("[Builder], useful for chaining.")*/)
            .addParameter(ParameterSpec.builder("value", type)
                //.addKdoc("The new value")
                .build())
            .addCode("""
            |${argument.normalizedName} = value
            |return this
            """.trimMargin())

        spec.addFunction(function.build())

        /* add parameter to constructor call in builder. */
        val constructorArgument = CodeBlock.of(argument.normalizedName)

        builder.addCode(
            argument.type.convert?.takeIf { argument.type.internalType != argument.type.exposedType } ?: "%L",
            constructorArgument
        )

        builder.addCode(", ")
    }

    spec.addFunction(builder
        .addCode(")")
        .build())

    return addType(spec.build())
}

fun TypeSpec.Builder.amqpMethodCompanionObject(amqpMethod: AMQP.Method): TypeSpec.Builder {
    val spec = TypeSpec.companionObjectBuilder()
        .addModifiers(KModifier.PUBLIC)
        .addFunction(
            FunSpec.builder("invoke")
                .addModifiers(KModifier.PUBLIC, KModifier.SUSPEND, KModifier.OPERATOR)
                .addParameter("reader", METHOD_PROTOCOL_READER)
                .amqpReadArguments(amqpMethod)
                .returns(ClassName("", amqpMethod.normalizedName))
                .build()
        )
        .addFunction(amqpCompanionBuilderFunction(ClassName("", amqpMethod.normalizedName)))
        .build()

    return addType(spec)
}

fun FunSpec.Builder.amqpReadArguments(amqpMethod: AMQP.Method): FunSpec.Builder {
    addCode("""
    |return ${amqpMethod.normalizedName}(
    ${amqpMethod.arguments.joinToString(",\n") { "|   reader.${it.type.readerMethod}()" }}
    |)
    """.trimMargin())

    return this
}

fun FunSpec.Builder.amqpWriteArguments(method: AMQP.Method): FunSpec.Builder {
    for (amqpArgument in method.arguments) {
        addCode("writer.${amqpArgument.type.writerMethod}(${amqpArgument.normalizedName})\n")
    }

    return this
}

fun TypeSpec.Builder.overrideMethod(
    name: String,
    type: TypeName,
    block: FunSpec.Builder.() -> Unit = {},
): TypeSpec.Builder {
    val spec = FunSpec.builder(name)
        .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
        .returns(type)
        .apply(block)
        .build()

    return addFunction(spec)
}

fun TypeSpec.Builder.amqpMethodPrimaryConstructor(amqpMethod: AMQP.Method): TypeSpec.Builder {
    /* create primary constructor. */
    val spec = FunSpec.constructorBuilder()
    for (amqpArgument in amqpMethod.arguments) {
        spec.addParameter(amqpArgument.normalizedName, amqpArgument.type.internalType)
    }

    primaryConstructor(spec.build())

    /* create properties with initializers. */
    for (amqpArgument in amqpMethod.arguments) {
        val prop = PropertySpec.builder(amqpArgument.normalizedName, amqpArgument.type.internalType)
            .addModifiers(KModifier.PUBLIC)
            .initializer(amqpArgument.normalizedName)
            .build()

        addProperty(prop)
    }

    return this
}
