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
    implementation(kotlin("gradle-plugin", version = "1.7.10"))

    implementation(gradleApi())
    implementation(localGroovy())

    /* misc */
//    implementation("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.17.3")
    implementation("fun.dimensional.gradle:gradle-tools:1.0.3")

    /* used for code-generation */
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup:kotlinpoet:1.12.0")
}
