package app.simplecloud.prefixes.paper.listener

import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import app.simplecloud.prefixes.paper.display.PaperTablist
import app.simplecloud.prefixes.shared.sync.SyncPublisher
import app.simplecloud.prefixes.shared.utilities.PlayerDisplayFormatter
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(
    private val manager: PaperDisplayManager,
    private val tablist: PaperTablist,
    private val publisher: SyncPublisher?
) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        tablist.remove(event.player.uniqueId)
        manager.addPlayer(event.player)
        manager.syncPlayers(event.player)
        tablist.sync(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        manager.removePlayer(event.player)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val data = manager.getPlayer(player.uniqueId) ?: return

        val message = PlayerDisplayFormatter.formatChatMessage(data, player.name, event.message())

        event.renderer { _, _, _, _ -> message }
        publisher?.publishChatMessage(message)
    }
}
