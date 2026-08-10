plugins {
    alias(libs.plugins.protobuf)
}

dependencies {
    api(project(":prefixes-api"))
    api(libs.simplecloud.plugin.api)
    implementation(libs.jnats)
    implementation(libs.protobuf.kotlin)
    implementation(libs.bundles.configurate)
    implementation(libs.bundles.cloud.command)
    testImplementation(libs.luckperms.api)
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
                create("kotlin")
            }
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-java:4.35.1")
    }
}
