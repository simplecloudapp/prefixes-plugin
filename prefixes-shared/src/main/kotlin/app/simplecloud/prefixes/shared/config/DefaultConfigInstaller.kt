package app.simplecloud.prefixes.shared.config

import java.nio.file.Files
import java.nio.file.Path

object DefaultConfigInstaller {

    private const val CONFIG_RESOURCE = "config.yml"

    fun install(target: Path, classLoader: ClassLoader) {
        if (Files.exists(target)) return

        val resource = checkNotNull(classLoader.getResourceAsStream(CONFIG_RESOURCE)) {
            "Missing bundled $CONFIG_RESOURCE"
        }

        Files.createDirectories(target.parent)
        resource.use { Files.copy(it, target) }
    }
}
