package app.simplecloud.prefixes.shared.utilities

import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage

fun parseColor(color: String): TextColor? {
    if (color.isEmpty()) return null
    val trimmed = color.trim()
    if (trimmed.startsWith("#")) {
        return TextColor.fromHexString(trimmed)
    }
    if (trimmed.startsWith("<#") && trimmed.endsWith(">")) {
        return TextColor.fromHexString(trimmed.substring(1, trimmed.length - 1))
    }
    val parsed = MiniMessage.miniMessage().deserialize(trimmed)
    return parsed.color() ?: parsed.style().color()
}

fun PrefixesPlayerData.getPriorityString(): String {
    val inverted = (1000 - priority).coerceIn(0, 999)
    return String.format("%03d", inverted)
}