package app.simplecloud.prefixes.paper

import app.simplecloud.prefixes.api.PrefixesApi
import app.simplecloud.prefixes.paper.command.PrefixesPaperSenderMapper
import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import app.simplecloud.prefixes.paper.display.PaperTablist
import app.simplecloud.prefixes.paper.listener.LuckPermsListener
import app.simplecloud.prefixes.paper.listener.PlayerListener
import app.simplecloud.prefixes.paper.platform.PaperPlatformImpl
import app.simplecloud.prefixes.paper.platform.PaperPrefixesListener
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.command.PrefixesCommand
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import space.chunks.customname.paper.CustomNameManagerImpl

class PrefixesPaper : JavaPlugin() {

    private var prefixes: Prefixes? = null

    override fun onEnable() {
        val customNameManager = CustomNameManagerImpl(this)
        customNameManager.registerListeners()

        val platform = PaperPlatformImpl(this)
        val prefixes = Prefixes(platform)
        this.prefixes = prefixes

        prefixes.startup()

        Bukkit.getServicesManager().register(PrefixesApi::class.java, prefixes.api, this, ServicePriority.Normal)

        val manager = PaperDisplayManager(this, prefixes, customNameManager)
        val tablist = PaperTablist()
        Bukkit.getPluginManager().registerEvents(PlayerListener(prefixes, manager, tablist), this)

        registerSync(prefixes, manager, tablist)
        registerLuckPermsListener(platform, manager)

        prefixes.addListener(PaperPrefixesListener(this, prefixes, manager, tablist))

        registerCommands(prefixes)
        prefixes.api.refreshAll()
    }

    override fun onDisable() {
        prefixes?.shutdown()
    }

    private fun registerSync(prefixes: Prefixes, manager: PaperDisplayManager, tablist: PaperTablist) {
        val sync = prefixes.sync ?: return

        sync.subscriber.subscribeChatMessage { message -> Bukkit.getServer().sendMessage(message) }
        sync.subscriber.subscribeTablist(tablist::update, tablist::remove) { Bukkit.getScheduler().runTask(this, Runnable { manager.publishAll(true) }) }
        sync.publisher.publishTablistRequest()

        Bukkit.getScheduler().runTaskTimer(this, Runnable { manager.publishAll() }, 600L, 600L)
    }

    private fun registerLuckPermsListener(platform: PrefixesPlatform, manager: PaperDisplayManager) {
        val luckPerms = platform.getLuckPerms() ?: return
        LuckPermsListener(this, luckPerms, manager).register()
    }

    private fun registerCommands(prefixes: Prefixes) {
        val manager = PaperCommandManager.builder(PrefixesPaperSenderMapper())
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
            .buildOnEnable(this)

        PrefixesCommand(manager, prefixes).register()
    }
}
