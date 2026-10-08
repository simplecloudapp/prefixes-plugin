package app.simplecloud.prefixes.shared.config

import java.nio.file.Files
import java.nio.file.Path

object DefaultConfigInstaller {

    fun install(target: Path, classLoader: ClassLoader) {
        if (Files.exists(target)) return

        val name = target.fileName.toString()
        val resource = checkNotNull(classLoader.getResourceAsStream(name)) {
            "Missing bundled $name"
        }

        Files.createDirectories(target.parent)
        resource.use { Files.copy(it, target) }
    }
}
