package app.simplecloud.prefixes.shared.utilities

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object PlayerDisplayFormatter {

    fun formatDisplayName(data: PrefixesPlayerData, playerName: String, displayNameEnabled: Boolean): Component {
        return if (displayNameEnabled) data.displayName else Component.text(playerName)
    }

    fun formatTablistName(data: PrefixesPlayerData, playerName: String): Component {
        return data.prefix
            .append(Component.text(playerName, data.color))
            .append(data.suffix)
    }

    fun formatChatMessage(data: PrefixesPlayerData, playerName: String, message: Component, displayNameEnabled: Boolean): Component {
        return miniMessage.deserialize(
            data.chatFormat,
            Placeholder.component("prefix", data.prefix),
            Placeholder.component("suffix", data.suffix),
            Placeholder.styling("color", data.color),
            Placeholder.unparsed("playername", playerName),
            Placeholder.unparsed("name", playerName),
            Placeholder.component("displayname", formatDisplayName(data, playerName, displayNameEnabled)),
            Placeholder.component("message", message)
        )
    }
}
