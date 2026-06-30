import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
}

allprojects {
    group = "app.simplecloud.plugin"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://oss.sonatype.org/content/repositories/central")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.simplecloud.app/snapshots")
        maven("https://buf.build/gen/maven")
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.gradleup.shadow")
    }

    dependencies {
        implementation(rootProject.libs.kotlin.jvm)
        implementation(rootProject.libs.kotlinx.coroutines.core)
        testImplementation(rootProject.libs.kotlin.test)
    }

    kotlin {
        jvmToolchain(25)
        compilerOptions {
            jvmTarget = JvmTarget.JVM_25
            languageVersion = KotlinVersion.KOTLIN_2_4
            apiVersion = KotlinVersion.KOTLIN_2_4
        }
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.shadowJar {
        mergeServiceFiles()
        archiveFileName.set("${project.name}.jar")
    }

}