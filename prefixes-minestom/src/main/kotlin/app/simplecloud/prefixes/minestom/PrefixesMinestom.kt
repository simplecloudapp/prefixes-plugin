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
import app.simplecloud.prefixes.shared.config.LUCKPERMS_SOURCE
import net.luckperms.api.LuckPerms
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.CommandSender
import net.minestom.server.event.EventNode
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minestom.MinestomCommandManager
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiPredicate
import java.util.logging.Logger

class PrefixesMinestom internal constructor(
    directory: Path,
    permissionHandler: BiPredicate<CommandSender, String>?,
    private val commands: Boolean,
    luckPerms: LuckPerms?
) {

    private val logger = Logger.getLogger("simplecloud-prefixes")
    private val node = EventNode.all("simplecloud-prefixes")
    private val permissions = MinestomPermissions(permissionHandler)
    private val platform = MinestomPlatformImpl(directory, permissions.getChecker(), luckPerms)
    private val prefixes = Prefixes(platform)
    private val manager = MinestomDisplayManager(prefixes, permissions)
    private val tablist = MinestomTablist()
    private val enabled = AtomicBoolean()

    private var syncTask: Task? = null

    /**
     * Gets the [PrefixesApi] instance.
     */
    fun getApi(): PrefixesApi = prefixes.api

    fun enable(): PrefixesMinestom {
        check(enabled.compareAndSet(false, true)) { "PrefixesMinestom is already enabled" }

        prefixes.startup()
        prefixes.addListener(MinestomPrefixesListener(prefixes, manager, tablist))

        PlayerListener(prefixes, manager, tablist).register(node)
        MinecraftServer.getGlobalEventHandler().addChild(node)

        registerSync()
        registerLuckPermsListener()
        registerCommands()

        MinecraftServer.getSchedulerManager().buildShutdownTask { disable() }
        return this
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

        syncTask = MinecraftServer.getSchedulerManager()
            .buildTask { manager.sync() }
            .delay(TaskSchedule.tick(600))
            .repeat(TaskSchedule.tick(600))
            .schedule()
    }

    fun disable() {
        if (!enabled.compareAndSet(true, false)) return

        MinecraftServer.getGlobalEventHandler().removeChild(node)
        syncTask?.cancel()
        manager.clear()
        tablist.clear()
        prefixes.shutdown()
    }

    private fun registerLuckPermsListener() {
        val source = prefixes.config.get().general.source
        logger.info("Using Source Type: $source")
        if (!source.equals(LUCKPERMS_SOURCE, ignoreCase = true)) return

        val luckPerms = platform.getLuckPerms()
        if (luckPerms == null) {
            logger.warning("Source Type is set to $LUCKPERMS_SOURCE, but LuckPerms was not found on the server!")
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
         * Creates a builder for a new instance.
         *
         * @param directory The directory the config files are stored in
         */
        @JvmStatic
        fun builder(directory: Path): PrefixesMinestomBuilder = PrefixesMinestomBuilder(directory)
    }
}
