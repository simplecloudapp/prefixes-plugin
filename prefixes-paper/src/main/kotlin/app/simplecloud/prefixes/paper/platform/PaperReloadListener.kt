package app.simplecloud.prefixes.paper.platform

import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import app.simplecloud.prefixes.paper.display.PaperTablist
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.platform.PrefixesReloadListener
import org.bukkit.Bukkit

class PaperReloadListener(
    private val prefixes: Prefixes,
    private val manager: PaperDisplayManager,
    private val tablist: PaperTablist
) : PrefixesReloadListener {

    override fun onReload() {
        Bukkit.getOnlinePlayers().forEach { player ->
            manager.updatePlayer(player)
        }

        val config = prefixes.config.get()
        if (!config.features.tablist || !config.sync.enabled || !config.sync.channels.tablist) {
            tablist.clear()
            return
        }

        prefixes.sync?.publisher?.publishTablistRequest()
    }
}