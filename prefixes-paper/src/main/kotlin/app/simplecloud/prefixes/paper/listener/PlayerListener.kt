package app.simplecloud.prefixes.paper.listener

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(
    private val manager: PaperDisplayManager
) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        manager.updatePlayer(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        manager.removePlayer(event.player)
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val data = manager.getPlayer(player.uniqueId) ?: return

        event.renderer { source, _, message, _ ->
            val placeholders = listOf(
                Placeholder.component("prefix", data.prefix),
                Placeholder.component("suffix", data.suffix),
                Placeholder.styling("color", data.color),
                Placeholder.unparsed("playername", source.name),
                Placeholder.unparsed("name", source.name),
                Placeholder.component("displayname", data.displayName),
                Placeholder.component("message", message)
            )

            miniMessage.deserialize(data.chatFormat, *placeholders.toTypedArray())
        }
    }
}
