package app.simplecloud.prefixes.shared.utilities

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage

object ColorParser {

    fun parse(color: String): TextColor? {
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

    fun serialize(color: TextColor?): String {
        if (color == null) return ""
        return "<${color.asHexString().uppercase()}>"
    }
}
