import codegen.GenerateAMQPClasses
import lol.dimensional.gradle.dsl.PreRelease
import lol.dimensional.gradle.dsl.PreReleaseType
import lol.dimensional.gradle.dsl.Version

plugins {
    `maven-publish`

    id("org.jetbrains.dokka") version "1.8.10"

    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.8.20"
}

val versionRef = Version(1, 0, 0, PreRelease(PreReleaseType.ReleaseCandidate, "1"))
version = "$versionRef"
group = "fun.dimensional"

repositories {
    mavenCentral()

    maven(url = "https://maven.dimensional.fun/releases")
    maven(url = "https://jitpack.io")
}

kotlin {
    explicitApi()

    jvm {
        withJava()

        compilations.all {
            kotlinOptions.jvmTarget = compiler.jvm.target
        }

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    jvmToolchain {
        languageVersion by JavaLanguageVersion.of(11)
    }

    linuxX64()

    sourceSets {
        all {
            for (optin in compiler.optins.kotlin) languageSettings.optIn(optin)
        }

        val commonMain by getting {
            kotlin.srcDir("src/commonGenerated")

            dependencies {
                /* Kotlin-specific things */
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

                // Coroutine Task Scheduling
                implementation("fun.dimensional:kyuso:1.1.0")

                // Idiomatic Logging for Kotlin
                implementation("io.github.microutils:kotlin-logging:3.0.5")

                /* Ktor */
                val ktor = "2.3.0"

                // TCP Sockets
                implementation("io.ktor:ktor-network:$ktor")
                implementation("io.ktor:ktor-network-tls:$ktor")

                // `Url` class
                implementation("io.ktor:ktor-http:$ktor")
            }
        }

        getByName("jvmTest").dependencies {
            implementation("com.rabbitmq:amqp-client:5.15.0")
            implementation("ch.qos.logback:logback-classic:1.2.11")
        }
    }
}

tasks {
    val jvmMainClasses by named("jvmMainClasses") {
        dependsOn("compileJava")
    }

    val jvmTestClasses by named("jvmTestClasses") {
        dependsOn("compileJava")
    }
    
    create<GenerateAMQPClasses>("generateAmqpClasses") {
        outputDirectory by file("src/commonGenerated")
    }

    val username = System.getenv("REPO_ALIAS")
    val password = System.getenv("REPO_TOKEN")

    if (username != null && password != null) publishing {
        repositories {
            dimensionalFun(versionRef.repository, username, password, true)
        }

        publications.filterIsInstance<MavenPublication>().forEach { publication ->
            publication.pom {
                name by project.name
                description by "\uD83D\uDC30 kotlin multi-platform rabbitmq client"
                url by "https://github.com/dimensional-fun/usagi"

                organization {
                    name by "Dimensional Fun"
                    url by "https://www.dimensional.fun"
                }

                developers {
                    developer {
                        name by "Dimensional Fun"
                        email by "opensource@dimensional.fun"
                        url by "https://opensource.dimensional.fun"
                    }

                    developer {
                        name by "melike2d"
                        email by "hi@2d.gay"
                        url by "https://2d.gay"
                    }
                }

                licenses {
                    license {
                        name by "Apache-2.0"
                        url by "https://opensource.org/licenses/Apache-2.0"
                    }
                }

                issueManagement {
                    system by "GitHub"
                    url by "https://github.com/dimensional-fun/usagi/issues"
                }

                scm {
                    connection by "scm:git:ssh://github.com/dimensional-fun/usagi.git"
                    developerConnection by "scm:git:ssh://git@github.com/dimensional-fun/usagi.git"
                    url by "https://github.com/dimensional-fun/usagi"
                }
            }
        }
    }
}

