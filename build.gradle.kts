import codegen.GenerateAMQPClasses

plugins {
    kotlin("multiplatform")

    kotlin("plugin.serialization") version "1.7.10"
}

allprojects {
    group = "mixtape.oss.usagi"

    repositories {
        mavenCentral()

        maven(url = "https://maven.dimensional.fun/releases")
        maven(url = "https://jitpack.io")
    }
}

tasks {
    create<GenerateAMQPClasses>("generateAmqpClasses") {
        outputDirectory.set(file("src/commonGenerated"))
    }
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = compiler.jvm.target
        }

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11)) // "8"
    }

    linuxX64()

    sourceSets {
        all {
            for (optin in compiler.optins.kotlin) languageSettings.optIn(optin)
        }

        val commonMain by getting {
            kotlin.srcDir("src/commonGenerated")

            dependencies {
                implementation(libs.bundles.common)
                implementation(libs.ktor.network)
                implementation(libs.kyuso)
            }
        }

        getByName("jvmTest").dependencies {
            implementation(libs.logback)
            implementation("com.rabbitmq:amqp-client:5.15.0")
        }
    }
}
