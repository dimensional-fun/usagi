plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    maven("https://maven.dimensional.fun/releases")
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    /* gradle bullshit */
    implementation(kotlin("gradle-plugin", version = "1.8.20"))

    implementation(gradleApi())
    implementation(localGroovy())

    /* misc */
    implementation("fun.dimensional.gradle:gradle-tools:2.0.0")

    /* used for code-generation */
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup:kotlinpoet:1.13.0")
}
