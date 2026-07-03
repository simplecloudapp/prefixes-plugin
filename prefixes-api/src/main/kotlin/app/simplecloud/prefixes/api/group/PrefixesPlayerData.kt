package app.simplecloud.prefixes.api.group

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

/**
 * Represents the prefixes data of a player.
 */
data class PrefixesPlayerData(
    val prefix: Component,
    val suffix: Component,
    val color: TextColor,
    val displayName: Component,
    val chatFormat: String,
    val priority: Int
)