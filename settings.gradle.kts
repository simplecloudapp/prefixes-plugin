plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    "prefixes-api",
    "prefixes-minestom",
    "prefixes-paper",
    "prefixes-shared"
)

rootProject.name = "prefixes"