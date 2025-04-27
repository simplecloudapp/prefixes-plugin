package app.simplecloud.plugin.prefixes.paper

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class PrefixesPlugin : JavaPlugin(), Listener {
    override fun onEnable() {
        val loader = PaperPrefixesLoader(this)
        if (loader.load() == null) {
            throw NullPointerException("The Prefixes Plugin could not load correctly")
        }
    }
}