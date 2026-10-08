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
import app.simplecloud.prefixes.shared.PrefixesConstants
import app.simplecloud.prefixes.shared.utilities.PrefixesCoroutineDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.luckperms.api.LuckPerms
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.entity.Player
import net.minestom.server.event.EventNode
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minestom.MinestomCommandManager
import space.chunks.customname.minestom.CustomNamesMinestom
import java.nio.file.Path
import java.util.function.BiPredicate
import kotlin.time.Duration.Companion.seconds

class PrefixesMinestom internal constructor(
    directory: Path,
    luckPerms: LuckPerms?,
    groupPermission: BiPredicate<Player, String>?,
    private val registerCommands: Boolean,
    commandPermission: BiPredicate<Player, String>?
) {

    private val node = EventNode.all("simplecloud-prefixes")
    private val permissions = MinestomPermissions(groupPermission, commandPermission)
    private val platform = MinestomPlatformImpl(directory, permissions.getChecker(), luckPerms)
    private val prefixes = Prefixes(platform)
    private val manager = MinestomDisplayManager(prefixes, CustomNamesMinestom.getManager())
    private val tablist = MinestomTablist()
    private val logger = PrefixesConstants.LOGGER

    /** Gets the [PrefixesApi] instance. */
    fun getApi(): PrefixesApi = prefixes.api

    fun init(): PrefixesMinestom {
        CustomNamesMinestom.init()
        prefixes.startup()
        prefixes.addListener(MinestomPrefixesListener(prefixes, manager, tablist))

        PlayerListener(prefixes, manager, tablist).register(node)
        MinecraftServer.getGlobalEventHandler().addChild(node)

        if (prefixes.config.get().general.source.equals(PrefixesConstants.CONFIG_SOURCE, ignoreCase = true) && !permissions.hasGroupPermission) {
            logger.warn("Source Type is set to '${PrefixesConstants.CONFIG_SOURCE}', but no group permission check was registered!")
        }

        registerSync()
        registerLuckPermsListener()
        registerCommands()
        prefixes.api.refreshAll()
        return this
    }

    fun shutdown() {
        MinecraftServer.getGlobalEventHandler().removeChild(node)
        prefixes.shutdown()
        CustomNamesMinestom.shutdown()
    }

    private fun registerSync() {
        val sync = prefixes.sync ?: return

        sync.subscriber.subscribeChatMessage { message -> Audiences.players().sendMessage(message) }
        sync.subscriber.subscribeTablist(
            onUpdate = tablist::update,
            onRemove = { publisherId, id -> tablist.remove(publisherId, id) },
            onRequest = { MinecraftServer.getSchedulerManager().scheduleNextTick { manager.publishAll(true) } }
        )
        sync.publisher.publishTablistRequest()

        PrefixesConstants.SCOPE.launch(PrefixesCoroutineDetails(null, "publish tablist")) {
            while (isActive) {
                delay(30.seconds)
                MinecraftServer.getSchedulerManager().scheduleNextTick { manager.publishAll() }
            }
        }
    }

    private fun registerLuckPermsListener() {
        val luckPerms = platform.getLuckPerms() ?: return
        LuckPermsListener(luckPerms, manager).register()
    }

    private fun registerCommands() {
        if (!registerCommands) return

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
