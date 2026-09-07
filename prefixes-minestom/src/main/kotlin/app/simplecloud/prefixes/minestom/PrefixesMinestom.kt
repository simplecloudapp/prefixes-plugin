package app.simplecloud.prefixes.minestom

import app.simplecloud.prefixes.api.PrefixesApi
import app.simplecloud.prefixes.minestom.command.PrefixesMinestomSenderMapper
import app.simplecloud.prefixes.minestom.display.MinestomDisplayManager
import app.simplecloud.prefixes.minestom.display.MinestomTablist
import app.simplecloud.prefixes.minestom.listener.LuckPermsListener
import app.simplecloud.prefixes.minestom.listener.PlayerListener
import app.simplecloud.prefixes.minestom.permission.MinestomPermissions
import app.simplecloud.prefixes.minestom.platform.MinestomPlatformImpl
import app.simplecloud.prefixes.minestom.platform.MinestomPrefixesListener
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.command.PrefixesCommand
import app.simplecloud.prefixes.shared.utilities.Constants
import net.luckperms.api.LuckPerms
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.CommandSender
import net.minestom.server.event.EventNode
import net.minestom.server.timer.TaskSchedule
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minestom.MinestomCommandManager
import java.nio.file.Path
import java.util.function.BiPredicate

class PrefixesMinestom internal constructor(
    directory: Path,
    private val permissionHandler: BiPredicate<CommandSender, String>?,
    private val commands: Boolean,
    luckPerms: LuckPerms?
) {

    private val node = EventNode.all("simplecloud-prefixes")
    private val permissions = MinestomPermissions(permissionHandler)
    private val platform = MinestomPlatformImpl(directory, permissions.getChecker(), luckPerms)
    private val prefixes = Prefixes(platform)
    private val manager = MinestomDisplayManager(prefixes)
    private val tablist = MinestomTablist()
    private val logger = prefixes.getPlatform().getLogger()

    /** Gets the [PrefixesApi] instance. */
    fun getApi(): PrefixesApi = prefixes.api

    fun enable(): PrefixesMinestom {
        prefixes.startup()
        prefixes.addListener(MinestomPrefixesListener(prefixes, manager, tablist))

        PlayerListener(prefixes, manager, tablist).register(node)
        MinecraftServer.getGlobalEventHandler().addChild(node)

        if (prefixes.config.get().general.source.equals(Constants.CONFIG_SOURCE, ignoreCase = true) && permissionHandler == null) {
            logger.warn("Source Type is set to '${Constants.CONFIG_SOURCE}', but no permission handler was registered!")
        }

        registerSync()
        registerLuckPermsListener()
        registerCommands()

        MinecraftServer.getSchedulerManager().buildShutdownTask { disable() }
        return this
    }

    private fun disable() {
        MinecraftServer.getGlobalEventHandler().removeChild(node)
        manager.clear()
        tablist.clear()
        prefixes.shutdown()
    }


    private fun registerSync() {
        val sync = prefixes.sync ?: return
        val config = prefixes.config.get()

        if (config.features.chat && config.sync.channels.chat) {
            sync.subscriber.subscribeChatMessage { message -> Audiences.players().sendMessage(message) }
        }

        if (!config.features.tablist || !config.sync.channels.tablist) return

        sync.subscriber.subscribeTablist(
            onUpdate = tablist::update,
            onRemove = { publisherId, id -> tablist.remove(publisherId, id) },
            onRequest = { manager.sync(force = true) }
        )
        sync.publisher.publishTablistRequest()

        MinecraftServer.getSchedulerManager()
            .buildTask { manager.sync() }
            .delay(TaskSchedule.tick(600))
            .repeat(TaskSchedule.tick(600))
            .schedule()
    }

    private fun registerLuckPermsListener() {
        val source = prefixes.config.get().general.source
        logger.info("Using Source Type: $source")
        if (!source.equals(Constants.LUCKPERMS_SOURCE, ignoreCase = true)) return

        val luckPerms = platform.getLuckPerms()
        if (luckPerms == null) {
            logger.warn("Source Type is set to ${Constants.LUCKPERMS_SOURCE}, but LuckPerms was not found on the server!")
            return
        }

        LuckPermsListener(luckPerms, manager).register()
    }

    private fun registerCommands() {
        if (!commands) return

        val commandManager = MinestomCommandManager(
            ExecutionCoordinator.asyncCoordinator(),
            PrefixesMinestomSenderMapper(permissions),
            permissions::hasPermission
        )

        PrefixesCommand(commandManager, prefixes).register()
    }

    companion object {
        /**
         * Creates a [PrefixesMinestomBuilder].
         *
         * @param directory The directory the config files are created in
         */
        @JvmStatic
        fun builder(directory: Path): PrefixesMinestomBuilder = PrefixesMinestomBuilder(directory)
    }
}
