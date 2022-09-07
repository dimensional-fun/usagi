rootProject.name = "usagi"

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            application()
            net()
            common()
        }
    }
}

/* application-specific libraries */
fun VersionCatalogBuilder.application() {
    /* logging */
    library("kotlin-logging", "io.github.microutils", "kotlin-logging").version("2.1.23")
    library("logback",        "ch.qos.logback",       "logback-classic").version("1.2.11")
}

/* common libraries */
fun VersionCatalogBuilder.common() {
    library("kyuso", "mixtape.oss", "kyuso").version("1.0.2")

    library("kotlin-stdlib",      "org.jetbrains.kotlin",  "kotlin-stdlib").version("1.7.10")
    library("kotlinx-datetime",   "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.4.0")
    library("kotlinx-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.6.4")

    bundle("common", listOf(
        "kotlin-stdlib",
        "kotlinx-coroutines",
        "kotlinx-datetime",
        "kotlin-logging"
    ))
}

/* networking */
fun VersionCatalogBuilder.net() {
    library("ktor-network", "io.ktor", "ktor-network").version("2.1.1")
}
