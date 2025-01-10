import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    java
}

allprojects {
    group = "app.simplecloud.plugin"
    version = "1.1-SNAPSHOT"

    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.github.johnrengelman.shadow")
    }

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://oss.sonatype.org/content/repositories/central")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.dmulloy2.net/repository/public/")
    }

    kotlin {
        jvmToolchain(17)
    }

    dependencies {
        compileOnly("net.luckperms:api:5.4")
        compileOnly("space.chunks.custom-names:custom-names-api:1.0.6")
        implementation("net.kyori:adventure-api:4.14.0")
        implementation("com.google.code.gson:gson:2.10.1")
        implementation("net.kyori:adventure-text-minimessage:4.14.0")
    }
}

subprojects {
    dependencies {
        implementation(kotlin("stdlib"))
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    tasks.named("shadowJar", ShadowJar::class) {
        mergeServiceFiles()
    }
}