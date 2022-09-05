package codegen

import com.squareup.kotlinpoet.*

fun TypeSpec.Builder.amqpToBuilderFunction(parent: AMQP.Parent): TypeSpec.Builder {
    val toBuilder = FunSpec.builder("toBuilder")
        .addModifiers(KModifier.PUBLIC)
        .returns(ClassName("", "Builder"))

    toBuilder.addCode("return Builder()\n")
    for (child in parent.children) {
        toBuilder.addCode("  .${child.normalizedName}(${child.normalizedName})\n")
    }

    return addFunction(toBuilder.build())
}


fun amqpCompanionBuilderFunction(returnType: TypeName): FunSpec {
    return FunSpec.builder("invoke")
        .addModifiers(KModifier.PUBLIC, KModifier.INLINE, KModifier.OPERATOR)
        .addParameter(
            ParameterSpec
            .builder("block", LambdaTypeName.get(ClassName("", "Builder"), returnType = UNIT))
            .defaultValue("{}")
            .build())
        .addCode("""
                |return Builder()
                |   .apply(block)
                |   .build()
                """.trimMargin())
        .returns(returnType)
        .build()
}
