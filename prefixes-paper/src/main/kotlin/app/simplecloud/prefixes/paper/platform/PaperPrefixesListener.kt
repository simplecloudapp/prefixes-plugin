package app.simplecloud.prefixes.paper.platform

import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import app.simplecloud.prefixes.paper.display.PaperTablist
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.platform.PrefixesListener
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.UUID

class PaperPrefixesListener(
    private val plugin: Plugin,
    private val prefixes: Prefixes,
    private val manager: PaperDisplayManager,
    private val tablist: PaperTablist
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
        val player = Bukkit.getPlayer(id) ?: return
        manager.updatePlayer(player)
    }

    override fun onPlayerDataChange(id: UUID) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            val player = Bukkit.getPlayer(id) ?: return@Runnable
            manager.render(player)
        })
    }

    override fun onAllPlayersUpdate() {
        Bukkit.getOnlinePlayers().forEach { player ->
            manager.updatePlayer(player)
        }
    }
}
