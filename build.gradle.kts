import codegen.GenerateAMQPClasses
import lol.dimensional.gradle.dsl.Version
import lol.dimensional.gradle.dsl.ReleaseType
import lol.dimensional.gradle.dsl.by

plugins {
    `maven-publish`

    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.7.10"
}

val versionRef = Version(0, 0, 1, release = ReleaseType.Snapshot)
version = "$versionRef"

allprojects {
    group = "mixtape.oss.usagi"

    repositories {
        mavenCentral()

        maven(url = "https://maven.dimensional.fun/releases")
        maven(url = "https://jitpack.io")
    }
}

extensions.configure<PublishingExtension> {
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
                    email by "oss@dimensional.fun"
                    url by "https://opensource.dimensional.fun"
                }

                developer {
                    name by "melike2d"
                    email by "gino@dimensional.fun"
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

tasks.create<GenerateAMQPClasses>("generateAmqpClasses") {
    outputDirectory by file("src/commonGenerated")
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
