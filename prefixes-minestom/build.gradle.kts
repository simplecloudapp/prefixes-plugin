dependencies {
    api(project(":prefixes-shared"))
    compileOnly(libs.minestom)
    compileOnly(libs.slf4j.api)
    implementation(libs.simplecloud.plugin.minestom)
    implementation(libs.cloud.command.minestom)
}

sourceSets {
    main {
        kotlin {
            srcDir(rootProject.layout.projectDirectory.dir("custom-names").dir("custom-names-api/src/main/kotlin"))
            srcDir(rootProject.layout.projectDirectory.dir("custom-names").dir("custom-names-minestom/src/main/kotlin"))
        }
    }
}