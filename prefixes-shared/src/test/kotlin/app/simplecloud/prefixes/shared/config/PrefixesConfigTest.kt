package app.simplecloud.prefixes.shared.config

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import java.nio.file.Files
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PrefixesConfigTest {

    @Test
    fun `installs and loads the documented version one config`() {
        val directory = Files.createTempDirectory("prefixes-config-test")
        val path = directory.resolve("config.yml")

        try {
            DefaultConfigInstaller.install(path, javaClass.classLoader)

            val yaml = path.readText()
            assertContains(yaml, "version: 1")
            assertContains(yaml, "# General")
            assertContains(yaml, "features:")
            assertContains(yaml, "display-name: true")
            assertContains(yaml, "# Sync")
            assertContains(yaml, "channels:")
            assertContains(yaml, "sources:")
            assertContains(yaml, "CURRENT")
            assertContains(yaml, "Special values:")
            assertContains(yaml, "# Groups")
            assertContains(yaml, "Supported placeholders:")

            val factory = ConfigurationFactory(path.toFile(), PrefixesConfig::class.java)
            val expected = PrefixesConfig()
            val loaded = factory.loadOrCreate(expected)

            assertEquals(expected, loaded)

            val customized = "$yaml\n# This user comment must be kept.\n"
            path.writeText(customized)
            DefaultConfigInstaller.install(path, javaClass.classLoader)
            assertEquals(customized, path.readText())
        } finally {
            Files.deleteIfExists(path)
            Files.deleteIfExists(directory)
        }
    }
}
