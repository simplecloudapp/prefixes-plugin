package app.simplecloud.prefixes.shared.utilities

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

val serializer: GsonComponentSerializer = GsonComponentSerializer.gson()

fun Int.getPriorityString(): String {
    val inverted = (1000 - this).coerceIn(0, 999)
    return String.format("%03d", inverted)
}

fun PrefixesPlayerData.getTablistName(): Component = prefix.append(displayName).append(suffix)

fun PrefixesPlayerData.renderChatMessage(playerName: String, message: Component): Component {
    return miniMessage.deserialize(
        chatFormat,
        Placeholder.component("prefix", prefix),
        Placeholder.component("suffix", suffix),
        Placeholder.styling("color", color),
        Placeholder.unparsed("playername", playerName),
        Placeholder.unparsed("name", playerName),
        Placeholder.component("displayname", displayName),
        Placeholder.component("message", message)
    )
}