dependencies {
    compileOnly(libs.simplecloud.api)
    api(libs.simplecloud.plugin.api)
    api(libs.log4j.api)
    implementation(libs.bundles.adventure)
    implementation(libs.bundles.configurate)
}