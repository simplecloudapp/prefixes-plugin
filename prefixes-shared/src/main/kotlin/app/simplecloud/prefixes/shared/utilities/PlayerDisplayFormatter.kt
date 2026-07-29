package app.simplecloud.prefixes.shared.utilities

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object PlayerDisplayFormatter {

    fun displayName(data: PrefixesPlayerData, playerName: String, enabled: Boolean): Component {
        return if (enabled) data.displayName else Component.text(playerName)
    }

    fun formatTablistName(data: PrefixesPlayerData, displayName: Component = data.displayName): Component {
        return data.prefix.append(displayName).append(data.suffix)
    }

    fun formatChatMessage(
        data: PrefixesPlayerData,
        playerName: String,
        message: Component,
        displayName: Component = data.displayName
    ): Component {
        return miniMessage.deserialize(
            data.chatFormat,
            Placeholder.component("prefix", data.prefix),
            Placeholder.component("suffix", data.suffix),
            Placeholder.styling("color", data.color),
            Placeholder.unparsed("playername", playerName),
            Placeholder.unparsed("name", playerName),
            Placeholder.component("displayname", displayName),
            Placeholder.component("message", message)
        )
    }
}
