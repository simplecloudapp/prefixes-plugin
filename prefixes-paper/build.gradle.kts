plugins {
    alias(libs.plugins.minotaur)
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    api(project(":prefixes-shared"))
    compileOnly(libs.paper.api)
    compileOnly(libs.custom.names.api)
    implementation(libs.cloud.command.paper)
    paperweight.paperDevBundle(libs.versions.paper)
}

tasks.shadowJar {
    relocate("com.google.protobuf", "app.simplecloud.prefixes.libs.protobuf")
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
        "1,21.11",
        "26.1",
        "26.1.1",
        "26.1.2",
        "26.2"
    )
    loaders.addAll("paper", "purpur")
    changelog.set("https://docs.simplecloud.app/changelog")
    syncBodyFrom.set(rootProject.file("README.md").readText())
}