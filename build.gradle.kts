import codegen.GenerateAMQPClasses
import lol.dimensional.gradle.dsl.Version
import lol.dimensional.gradle.dsl.ReleaseType
import lol.dimensional.gradle.dsl.by

plugins {
    `maven-publish`

    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.7.21"
}

val versionRef = Version(0, 0, 1, release = ReleaseType.Snapshot)
version = "$versionRef"
group = "dimensional.usagi"

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
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

                implementation("mixtape.oss:kyuso:1.0.2")

                implementation("io.github.microutils:kotlin-logging:2.1.23")

                implementation("io.ktor:ktor-network:2.1.3")
                implementation("io.ktor:ktor-network-tls:2.1.3")
                implementation("io.ktor:ktor-http:2.1.3")
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

    publishing {
        repositories {
            dimensionalFun(
                versionRef.repository,
                System.getenv("REPO_ALIAS"),
                System.getenv("REPO_TOKEN"),
                true
            )
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

