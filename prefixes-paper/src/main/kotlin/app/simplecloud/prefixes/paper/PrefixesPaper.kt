package app.simplecloud.prefixes.paper

import app.simplecloud.prefixes.shared.Prefixes
import org.bukkit.plugin.java.JavaPlugin

class PrefixesPaper : JavaPlugin() {

    private val prefixes = Prefixes()

    override fun onEnable() {
        prefixes.startup()
    }

    override fun onDisable() {
        prefixes.shutdown()
    }

}