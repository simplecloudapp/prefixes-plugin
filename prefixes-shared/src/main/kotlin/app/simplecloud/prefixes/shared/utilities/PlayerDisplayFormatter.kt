package app.simplecloud.prefixes.shared.utilities

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object PlayerDisplayFormatter {

    fun formatTablistName(data: PrefixesPlayerData): Component {
        return data.prefix.append(data.displayName).append(data.suffix)
    }

    fun formatChatMessage(data: PrefixesPlayerData, playerName: String, message: Component): Component {
        return miniMessage.deserialize(
            data.chatFormat,
            Placeholder.component("prefix", data.prefix),
            Placeholder.component("suffix", data.suffix),
            Placeholder.styling("color", data.color),
            Placeholder.unparsed("playername", playerName),
            Placeholder.unparsed("name", playerName),
            Placeholder.component("displayname", data.displayName),
            Placeholder.component("message", message)
        )
    }
}
