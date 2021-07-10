val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val main: String by project

plugins {
    application
    kotlin("jvm") version "1.5.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "cn.thelama"
version = "1.0"

application {
    mainClass.set(main)
    this.mainClassName = main
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.mongodb:mongodb-driver:3.12.8")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.16")
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        this.manifest.attributes["Main-Class"] = "cn.thelama.hent.yggdrasil.ApplicationKt"
    }
}