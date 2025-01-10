pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

include("prefixes-api", "prefixes-minestom", "prefixes-paper", "prefixes-shared")

rootProject.name = "Prefixes-Plugin"