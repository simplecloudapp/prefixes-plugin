package app.simplecloud.prefixes.shared.utilities

import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerDisplayFormatterTest {

    private val data = PrefixesPlayerData(
        prefix = Component.text("[Admin] "),
        suffix = Component.text(" !"),
        color = NamedTextColor.RED,
        displayName = Component.text("Fancy"),
        chatFormat = "<displayname>: <message>",
        priority = 90
    )

    @Test
    fun `uses the configured display name when enabled`() {
        assertEquals(
            Component.text("Fancy"),
            PlayerDisplayFormatter.displayName(data, "Player", enabled = true)
        )
    }

    @Test
    fun `uses the minecraft player name when display names are disabled`() {
        val displayName = PlayerDisplayFormatter.displayName(data, "Player", enabled = false)

        assertEquals(Component.text("Player"), displayName)
        assertEquals(
            Component.text("[Admin] ").append(displayName).append(Component.text(" !")),
            PlayerDisplayFormatter.formatTablistName(data, displayName)
        )
    }
}
