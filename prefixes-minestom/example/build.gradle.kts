plugins {
    application
}

dependencies {
    implementation(project(":prefixes-minestom"))
}

application {
    mainClass.set("app.simplecloud.prefixes.minestom.example.ExampleServerKt")
}
