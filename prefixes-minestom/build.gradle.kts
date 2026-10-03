dependencies {
    api(project(":prefixes-shared"))
    implementation(libs.minestom)
    implementation(libs.cloud.command.minestom)
    implementation(libs.slf4j.api)
}

sourceSets {
    main {
        kotlin {
            srcDir(rootProject.layout.projectDirectory.dir("custom-names").dir("custom-names-api/src/main/kotlin"))
            srcDir(rootProject.layout.projectDirectory.dir("custom-names").dir("custom-names-minestom/src/main/kotlin"))
        }
    }
}