dependencies {
    api(project(":prefixes-shared"))
    implementation(libs.minestom)
    implementation(libs.slf4j.api)
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