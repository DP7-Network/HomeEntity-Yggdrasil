val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val mainClassName = "cn.thelama.hent.yggdrasil.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "1.5.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "cn.thelama"
version = "1.0"

application {
    mainClass.set("cn.thelama.hent.yggdrasil.ApplicationKt")
    this.mainClassName = "cn.thelama.hent.yggdrasil.ApplicationKt"
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
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        this.manifest.attributes["Main-Class"] = "cn.thelama.hent.yggdrasil.ApplicationKt"
    }
}