plugins {
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    api(project(":prefixes-shared"))
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    compileOnly(libs.paper.api)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}