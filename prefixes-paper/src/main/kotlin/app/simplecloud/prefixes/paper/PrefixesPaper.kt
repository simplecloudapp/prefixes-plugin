package app.simplecloud.prefixes.paper

import app.simplecloud.prefixes.api.PrefixesApi
import app.simplecloud.prefixes.paper.command.PrefixesPaperSenderMapper
import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import app.simplecloud.prefixes.paper.display.PaperTablist
import app.simplecloud.prefixes.paper.listener.LuckPermsListener
import app.simplecloud.prefixes.paper.listener.PlayerListener
import app.simplecloud.prefixes.paper.platform.PaperPlatformImpl
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.command.PrefixesCommand
import app.simplecloud.prefixes.shared.config.SourceType
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import space.chunks.customname.plugin.CustomNameManagerImpl

class PrefixesPaper : JavaPlugin() {

    private var prefixes: Prefixes? = null

    override fun onEnable() {
        val customNameManager = CustomNameManagerImpl(this)
        customNameManager.registerListeners()

        val platform = PaperPlatformImpl(this)
        val prefixes = Prefixes(platform)
        this.prefixes = prefixes

        prefixes.startup()

        // Register Prefixes API as a Bukkit Service.
        Bukkit.getServicesManager().register(PrefixesApi::class.java, prefixes.api, this, ServicePriority.Normal)

        val manager = PaperDisplayManager(this, prefixes, customNameManager)
        val tablist = PaperTablist()
        Bukkit.getPluginManager().registerEvents(
            PlayerListener(prefixes, manager, tablist, prefixes.sync?.publisher),
            this
        )

        // Apply chat and tablist updates from the configured sync sources.
        prefixes.sync?.let { sync ->
            val config = prefixes.config.get()
            if (config.features.chat && config.sync.channels.chat) {
                sync.subscriber.subscribeChatMessage { message -> Bukkit.getServer().sendMessage(message) }
            }
            if (config.features.tablist && config.sync.channels.tablist) {
                sync.subscriber.subscribeTablist(
                    onUpdate = tablist::update,
                    onRemove = tablist::remove,
                    onRequest = { manager.sync(force = true) }
                )
                sync.publisher.publishTablistRequest()

                Bukkit.getScheduler().runTaskTimer(this, Runnable { manager.sync() }, 600L, 600L)
            }
        }

        val source = prefixes.config.get().general.source
        logger.info("Using Source Type: ${source.name}")

        // Register LuckPerms listener.
        if (source == SourceType.LUCKPERMS) {
            val lp = platform.luckPerms
            if (lp != null) {
                LuckPermsListener(this, lp, manager).register()
            } else {
                logger.warning("Source Type is set to LUCKPERMS, but LuckPerms was not found on the server!")
            }
        }

        // Refresh all online players on reload.
        prefixes.onReload = {
            val config = prefixes.config.get()
            Bukkit.getOnlinePlayers().forEach { player ->
                manager.updatePlayer(player)
            }

            if (!config.features.tablist || !config.sync.enabled || !config.sync.channels.tablist) {
                tablist.clear()
            } else {
                prefixes.sync?.publisher?.publishTablistRequest()
            }
        }

        registerCommands(prefixes)
    }

    override fun onDisable() {
        prefixes?.shutdown()
    }

    private fun registerCommands(prefixes: Prefixes) {
        val manager = PaperCommandManager.builder(PrefixesPaperSenderMapper())
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
            .buildOnEnable(this)

        PrefixesCommand(manager, prefixes).register()
    }
}
