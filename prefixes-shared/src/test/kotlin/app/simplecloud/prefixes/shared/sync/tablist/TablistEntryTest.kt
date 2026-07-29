package app.simplecloud.prefixes.shared.sync.tablist

import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class TablistEntryTest {

    @Test
    fun `serializes all player info`() {
        val entry = TablistEntry(
            uniqueId = UUID.fromString("c1688a0f-13e4-4b1c-82c6-7f576f4d9021"),
            name = "Player",
            displayName = Component.text("Displayed Player"),
            priority = 42,
            profileProperties = listOf(
                ProfileProperty("textures", "texture-value", "texture-signature"),
                ProfileProperty("unsigned", "value", null)
            ),
            latency = 123,
            gameMode = TablistGameMode.CREATIVE,
            showHat = false,
            listOrder = 7
        )

        assertEquals(entry, TablistEntryMapper.fromDefinition(TablistEntryMapper.toDefinition(entry)))
    }

    @Test
    fun `uses safe defaults for older sync messages`() {
        val definition = TablistEntry(
            uniqueId = UUID.fromString("b2460751-a9e8-4b4a-ace2-25b883231667"),
            name = "Player",
            displayName = Component.text("Player"),
            priority = 0
        ).let(TablistEntryMapper::toDefinition).toBuilder()
            .clearGameMode()
            .clearShowHat()
            .build()

        val entry = TablistEntryMapper.fromDefinition(definition)

        assertEquals(TablistGameMode.SURVIVAL, entry.gameMode)
        assertEquals(true, entry.showHat)
    }
}
