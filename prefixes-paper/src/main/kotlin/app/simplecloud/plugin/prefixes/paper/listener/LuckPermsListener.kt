package app.simplecloud.plugin.prefixes.paper.listener

import app.simplecloud.plugin.prefixes.api.PrefixesApi
import net.luckperms.api.LuckPerms
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.plugin.Plugin

class LuckPermsListener(
    private val plugin: Plugin,
    private val luckPerms: LuckPerms,
    private val api: PrefixesApi
) {

    fun init() {
        val eventBus = luckPerms.eventBus
        eventBus.subscribe(plugin, UserDataRecalculateEvent::class.java, this::onUserUpdate)
    }

    private val groups: MutableMap<String, String> = mutableMapOf()

    private fun onUserUpdate(event: UserDataRecalculateEvent) {
        if(groups.getOrDefault(event.user.uniqueId.toString(), "") == event.user.primaryGroup) return
        groups[event.user.uniqueId.toString()] = event.user.primaryGroup
        api.setWholeName(event.user.uniqueId, event.user.primaryGroup)
    }

}