package app.simplecloud.prefixes.api.util

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Parses a color from a MiniMessage tag or a plain hex string.
 *
 * @param color The color to parse
 * @return The parsed color, or `null` if [color] is empty or holds no color
 */
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
