package app.simplecloud.prefixes.shared.utilities

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage

object ColorParser {

    fun parse(color: String): TextColor? {
        val trimmed = color.trim()
        if (trimmed.isEmpty()) return null

        if (trimmed.startsWith("#")) {
            return TextColor.fromHexString(trimmed)
        }
        if (trimmed.startsWith("<#") && trimmed.endsWith(">")) {
            return TextColor.fromHexString(trimmed.substring(1, trimmed.length - 1))
        }

        val named = NamedTextColor.NAMES.value(trimmed.lowercase())
        if (named != null) return named

        return MiniMessage.miniMessage().deserialize(trimmed).color()
    }

    fun serialize(color: TextColor): String = "<${color.asHexString().uppercase()}>"
}
