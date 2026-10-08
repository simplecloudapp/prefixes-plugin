package app.simplecloud.prefixes.shared.utilities

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage

object ColorParser {

    fun parse(color: String): TextColor? {
        val value = color.trim().removePrefix("<").removeSuffix(">").lowercase()
        if (value.isEmpty()) return null

        if (value.startsWith("#")) {
            return TextColor.fromHexString(value)
        }

        return MiniMessage.miniMessage().deserialize("<$value>.").color()
    }

    fun serialize(color: TextColor): String = "<${color.asHexString().uppercase()}>"
}
