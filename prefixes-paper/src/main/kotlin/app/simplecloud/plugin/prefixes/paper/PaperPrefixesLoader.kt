package app.simplecloud.plugin.prefixes.paper

import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.api.PrefixesConfig
import app.simplecloud.plugin.prefixes.api.PrefixesConfigParser
import app.simplecloud.plugin.prefixes.paper.impl.LuckPermsPrefixesApiImpl
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.ServicePriority
import space.chunks.customname.api.CustomNameManager
import java.io.File

class PaperPrefixesLoader(
    private val plugin: Plugin,
) {

    private lateinit var api: LuckPermsPrefixesApiImpl
    fun load(): PrefixesApi<Player>? {
        val customNameManager = Bukkit.getServicesManager().load(CustomNameManager::class.java) ?: return null
        val luckPermsProvider: RegisteredServiceProvider<LuckPerms> =
            Bukkit.getServicesManager().getRegistration(LuckPerms::class.java) ?: return null
        val luckPerms: LuckPerms = luckPermsProvider.provider
        api = LuckPermsPrefixesApiImpl(luckPerms, plugin, customNameManager)
        api.setConfig(
            PrefixesConfigParser(File(plugin.dataFolder, "config.json")).parse(
                PrefixesConfig::class.java,
                PrefixesConfig()
            )
        )
        plugin.saveResource("config.json", false)
        api.indexGroups(api)
        Bukkit.getPluginManager().registerEvents(api, plugin)
        Bukkit.getServicesManager().register(PrefixesApi::class.java, api, plugin, ServicePriority.Normal)
        return api
    }
}