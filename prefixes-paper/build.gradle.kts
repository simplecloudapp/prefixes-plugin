plugins {
    alias(libs.plugins.minotaur)
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    api(project(":prefixes-shared"))
    compileOnly(libs.paper.api)
    implementation(libs.simplecloud.plugin.paper)
    implementation(libs.cloud.command.paper)
    implementation(libs.reflection.remapper)
    paperweight.paperDevBundle(libs.versions.paper)
}

sourceSets {
    main {
        kotlin {
            srcDir(rootProject.layout.projectDirectory.dir("custom-names").dir("custom-names-api/src/main/kotlin"))
            srcDir(rootProject.layout.projectDirectory.dir("custom-names").dir("custom-names-paper/src/main/kotlin"))
            exclude("**/CustomNamesPlugin.kt")
        }
    }
}

tasks.shadowJar {
    relocate("com.google.protobuf", "app.simplecloud.prefixes.libs.protobuf")
    relocate("space.chunks.customname", "app.simplecloud.prefixes.libs.customname")
}

modrinth {
    token.set(project.findProperty("modrinthToken") as String? ?: System.getenv("MODRINTH_TOKEN"))
    projectId.set("FZ0Sdplu")
    versionNumber.set(rootProject.version.toString())
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(
        "26.2",
        "26.3",
    )
    loaders.addAll("paper", "purpur", "folia")
    changelog.set("https://docs.simplecloud.app/changelog")
    syncBodyFrom.set(rootProject.file("README.md").readText())
}