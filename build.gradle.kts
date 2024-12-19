@file:Suppress("PropertyName", "VulnerableLibrariesLocal")

import io.ktor.plugin.features.*

val exposed_version: String by project
val h2_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val hoplite_version: String by project
val clikt_version: String by project
val postgre_version: String by project
val sqlite_version: String by project
val mysql_version: String by project
val argon2_version: String by project

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

group = "art.shittim"
version = "0.1.4"

application {
    mainClass.set("art.shittim.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()

    maven {
        name = "Tencent"
        url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    }

    maven {
        name = "Aliyun"
        url = uri("https://maven.aliyun.com/repository/central")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-csrf-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-resources-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-pebble")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-sessions")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_version")
    implementation("com.sksamuel.hoplite:hoplite-core:$hoplite_version")
    implementation("com.sksamuel.hoplite:hoplite-yaml:$hoplite_version")
    implementation("de.mkammerer:argon2-jvm:$argon2_version")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:$clikt_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("org.xerial:sqlite-jdbc:$sqlite_version")
    implementation("org.postgresql:postgresql:$postgre_version")
    implementation("mysql:mysql-connector-java:$mysql_version")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml-jvm")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_21)

        localImageName.set("classics-docker-image")
        imageTag.set(version.toString())

        externalRegistry.set(
            DockerImageRegistry.dockerHub(
                appName = provider { "yc2-classics" },
                username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
            )
        )
    }
}

sourceSets {
    main {
        resources.srcDirs("src/main/templates")
    }
}

tasks {
    processResources {

    }
}

kotlin {
    jvmToolchain(21)
}