dependencies {
    compileOnly(libs.minestom.ce.snapshots)
    compileOnly(libs.minestom.ce.extensions)
    api(project(":prefixes-api"))
    compileOnly(files("libs/lp-minestom-5.4-SNAPSHOT-all.jar"))
}