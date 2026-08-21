package app.simplecloud.prefixes.paper

import app.simplecloud.prefixes.api.PrefixesApi
import app.simplecloud.prefixes.paper.command.PrefixesPaperSenderMapper
import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import app.simplecloud.prefixes.paper.display.PaperTablist
import app.simplecloud.prefixes.paper.listener.LuckPermsListener
import app.simplecloud.prefixes.paper.listener.PlayerListener
import app.simplecloud.prefixes.paper.platform.PaperReloadListener
import app.simplecloud.prefixes.paper.platform.PaperPlatformImpl
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.command.PrefixesCommand
import app.simplecloud.prefixes.shared.config.SourceType
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
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
        Bukkit.getPluginManager().registerEvents(PlayerListener(prefixes, manager, tablist), this)

        registerSync(prefixes, manager, tablist)
        registerLuckPermsListener(prefixes, platform, manager)

        // Refresh all online players on reload.
        prefixes.addReloadListener(PaperReloadListener(prefixes, manager, tablist))

        registerCommands(prefixes)
    }

    override fun onDisable() {
        prefixes?.shutdown()
    }

    // Apply chat and tablist updates from the configured sync sources.
    private fun registerSync(prefixes: Prefixes, manager: PaperDisplayManager, tablist: PaperTablist) {
        val sync = prefixes.sync ?: return
        val config = prefixes.config.get()

        if (config.features.chat && config.sync.channels.chat) {
            sync.subscriber.subscribeChatMessage { message -> Bukkit.getServer().sendMessage(message) }
        }

        if (!config.features.tablist || !config.sync.channels.tablist) return

        sync.subscriber.subscribeTablist(
            onUpdate = tablist::update,
            onRemove = tablist::remove,
            onRequest = { manager.sync(force = true) }
        )
        sync.publisher.publishTablistRequest()

        Bukkit.getScheduler().runTaskTimer(
            this,
            Runnable { manager.sync() },
            600L,
            600L
        )
    }

    private fun registerLuckPermsListener(prefixes: Prefixes, platform: PrefixesPlatform, manager: PaperDisplayManager) {
        val source = prefixes.config.get().general.source
        logger.info("Using Source Type: ${source.name}")
        if (source != SourceType.LUCKPERMS) return

        val luckPerms = platform.getLuckPerms()
        if (luckPerms == null) {
            logger.warning("Source Type is set to LUCKPERMS, but LuckPerms was not found on the server!")
            return
        }

        LuckPermsListener(this, luckPerms, manager).register()
    }

    private fun registerCommands(prefixes: Prefixes) {
        val manager = PaperCommandManager.builder(PrefixesPaperSenderMapper())
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
            .buildOnEnable(this)

        PrefixesCommand(manager, prefixes).register()
    }
}
