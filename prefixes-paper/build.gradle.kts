plugins {
    alias(libs.plugins.minotaur)
    alias(libs.plugins.paperweight.userdev)
}

val customNamesRoot = rootProject.layout.projectDirectory.dir("custom-names")
require(
    customNamesRoot.dir("custom-names-api/src/main/kotlin").asFile.isDirectory &&
        customNamesRoot.dir("custom-names-plugin/src/main/kotlin").asFile.isDirectory
) {
    "The custom-names submodule is missing. Run `git submodule update --init --recursive`."
}

dependencies {
    api(project(":prefixes-shared"))
    compileOnly(libs.paper.api)
    implementation(libs.cloud.command.paper)
    implementation(libs.reflection.remapper)
    paperweight.paperDevBundle(libs.versions.paper)
}

sourceSets {
    main {
        kotlin {
            srcDir(customNamesRoot.dir("custom-names-api/src/main/kotlin"))
            srcDir(customNamesRoot.dir("custom-names-plugin/src/main/kotlin"))
            exclude("**/CustomNamesPlugin.kt")
        }
    }
}

tasks.shadowJar {
    relocate("com.google.protobuf", "app.simplecloud.prefixes.libs.protobuf")
    relocate("space.chunks.customname", "app.simplecloud.prefixes.libs.customname")
    relocate("xyz.jpenilla.reflectionremapper", "app.simplecloud.prefixes.libs.reflectionremapper")
    exclude("org/bouncycastle/jcajce/io/DigestUpdatingOutputStream.class")
}

modrinth {
    token.set(project.findProperty("modrinthToken") as String? ?: System.getenv("MODRINTH_TOKEN"))
    projectId.set("FZ0Sdplu")
    versionNumber.set(rootProject.version.toString())
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(
        "1.21",
        "1.21.1",
        "1.21.2",
        "1.21.3",
        "1.21.4",
        "1.21.5",
        "1.21.6",
        "1.21.7",
        "1.21.8",
        "1.21.9",
        "1.21.10",
        "1.21.11",
        "26.1",
        "26.1.1",
        "26.1.2",
        "26.2"
    )
    loaders.addAll("paper", "purpur")
    changelog.set("https://docs.simplecloud.app/changelog")
    syncBodyFrom.set(rootProject.file("README.md").readText())
}
