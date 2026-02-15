pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("prefixes-api", "prefixes-minestom", "prefixes-paper", "prefixes-shared")

rootProject.name = "prefixes"