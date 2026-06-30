plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    "prefixes-api",
    "prefixes-shared",
    "prefixes-paper"
)

rootProject.name = "prefixes"