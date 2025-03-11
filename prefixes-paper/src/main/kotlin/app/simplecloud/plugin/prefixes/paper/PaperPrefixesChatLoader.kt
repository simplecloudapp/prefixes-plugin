package app.simplecloud.plugin.prefixes.paper

import app.simplecloud.plugin.prefixes.api.PrefixesChatLoader
import app.simplecloud.plugin.prefixes.api.impl.PrefixesApiImpl
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.identity.Identity
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class PaperPrefixesChatLoader(private val plugin: Plugin) : PrefixesChatLoader, Listener {

    private lateinit var api: PrefixesApiImpl
    override fun load(api: PrefixesApiImpl) {
        this.api = api
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: AsyncChatEvent) {
        event.renderer { player, _, message, audience ->
            val viewer = audience.getOrDefault(Identity.UUID, null)
            api.formatChatMessage(player.uniqueId, viewer, api.getConfig().getChatFormat(), message)
        }
    }
}