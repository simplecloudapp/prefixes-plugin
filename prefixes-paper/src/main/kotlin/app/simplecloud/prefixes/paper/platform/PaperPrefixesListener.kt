package app.simplecloud.prefixes.paper.platform

import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import app.simplecloud.prefixes.paper.display.PaperTablist
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.platform.PrefixesListener
import org.bukkit.Bukkit
import java.util.UUID

class PaperPrefixesListener(
    private val prefixes: Prefixes,
    private val manager: PaperDisplayManager,
    private val tablist: PaperTablist
) : PrefixesListener {

    override fun onReload() {
        onAllPlayersUpdate()

        val config = prefixes.config.get()
        if (!config.features.tablist || !config.sync.enabled || !config.sync.channels.tablist) {
            tablist.clear()
            return
        }

        prefixes.sync?.publisher?.publishTablistRequest()
    }

    override fun onPlayerUpdate(id: UUID) {
        val player = Bukkit.getPlayer(id) ?: return
        manager.updatePlayer(player)
    }

    override fun onAllPlayersUpdate() {
        Bukkit.getOnlinePlayers().forEach { player ->
            manager.updatePlayer(player)
        }
    }
}
