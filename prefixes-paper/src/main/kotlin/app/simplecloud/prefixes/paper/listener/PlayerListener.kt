package app.simplecloud.prefixes.paper.listener

import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import app.simplecloud.prefixes.paper.display.PaperTablist
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.utilities.PlayerDisplayFormatter
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(
    private val prefixes: Prefixes,
    private val manager: PaperDisplayManager,
    private val tablist: PaperTablist
) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        tablist.remove(player.uniqueId)
        manager.addPlayer(player)
        manager.addViewer(player)

        if (prefixes.config.get().isTablistSynced()) {
            tablist.sync(player)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        manager.removePlayer(event.player)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        val features = prefixes.config.get().features
        if (!features.chat) return

        val player = event.player
        val data = prefixes.dataRegistry.getData(player.uniqueId) ?: return
        val message = PlayerDisplayFormatter.formatChatMessage(data, player.name, event.message(), features.displayName)

        event.renderer { _, _, _, viewer ->
            val viewerData = prefixes.dataRegistry.getViewerData(player.uniqueId, viewer)
            if (viewerData == null) message else PlayerDisplayFormatter.formatChatMessage(viewerData, player.name, event.message(), features.displayName)
        }
        prefixes.sync?.publisher?.publishChatMessage(message)
    }
}
