package app.simplecloud.prefixes.minestom.platform

import app.simplecloud.prefixes.minestom.display.MinestomDisplayManager
import app.simplecloud.prefixes.minestom.display.MinestomTablist
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.platform.PrefixesListener
import net.minestom.server.MinecraftServer
import java.util.UUID

class MinestomPrefixesListener(
    private val prefixes: Prefixes,
    private val manager: MinestomDisplayManager,
    private val tablist: MinestomTablist
) : PrefixesListener {

    override fun onReload() {
        onAllPlayersUpdate()

        if (!prefixes.config.get().isTablistSynced()) {
            tablist.clear()
            return
        }

        prefixes.sync?.publisher?.publishTablistRequest()
    }

    override fun onShutdown() {
        manager.clear()
        tablist.clear()
    }

    override fun onPlayerUpdate(id: UUID) {
        val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) ?: return
        manager.updatePlayer(player)
    }

    override fun onPlayerDataChange(id: UUID) {
        MinecraftServer.getSchedulerManager().scheduleNextTick {
            val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) ?: return@scheduleNextTick
            manager.render(player)
        }
    }

    override fun onAllPlayersUpdate() {
        MinecraftServer.getConnectionManager().onlinePlayers.forEach { player ->
            manager.updatePlayer(player)
        }
    }
}
