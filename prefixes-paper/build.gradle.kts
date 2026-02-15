plugins {
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.minotaur)
}

dependencies {
    api(project(":prefixes-api"))
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly(libs.paper.api)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}

modrinth {
    token.set(project.findProperty("modrinthToken") as String? ?: System.getenv("MODRINTH_TOKEN"))
    projectId.set("FZ0Sdplu")
    versionNumber.set(rootProject.version.toString())
    versionType.set("beta")
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
        "1.21.11"
    )
    loaders.add("paper")
    changelog.set("https://docs.simplecloud.app/changelog")
    syncBodyFrom.set(rootProject.file("README.md").readText())
}
