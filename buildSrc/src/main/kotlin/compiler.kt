object compiler {
    object jvm {
        const val target = "11"
    }

    object optins {
        val kotlin = listOf(
            "kotlin.RequiresOptIn",
            "kotlin.ExperimentalStdlibApi",
            "kotlin.ExperimentalUnsignedTypes",
            "kotlin.time.ExperimentalTime",
            "kotlin.contracts.ExperimentalContracts",
            "kotlinx.coroutines.ExperimentalCoroutinesApi",
            "kotlinx.serialization.InternalSerializationApi",
            "kotlinx.serialization.ExperimentalSerializationApi"
        )

        val all = kotlin
    }
}
