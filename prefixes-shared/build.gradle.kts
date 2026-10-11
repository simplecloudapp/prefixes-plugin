plugins {
    alias(libs.plugins.protobuf)
}

dependencies {
    api(project(":prefixes-api"))
    api(libs.simplecloud.plugin.api)
    implementation(libs.jnats) {
        // only needed for NKey
        exclude(group = "org.bouncycastle")
    }
    implementation(libs.slf4j.api)
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.bundles.configurate)
    implementation(libs.bundles.cloud.command)
}

sourceSets {
    main {
        kotlin {
            srcDirs(
                "build/generated/source/proto/main/kotlin",
            )
        }
        java {
            srcDirs(
                "build/generated/source/proto/main/java",
            )
        }
    }
}

protobuf {
    protoc {
        artifact = libs.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                named("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-javalite:4.36.2")
    }
}