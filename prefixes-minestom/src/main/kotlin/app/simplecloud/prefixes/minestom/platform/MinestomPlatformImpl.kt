package app.simplecloud.prefixes.minestom.platform

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.minestom.server.MinecraftServer
import java.io.File
import java.nio.file.Path
import java.util.UUID

class MinestomPlatformImpl(
    private val path: Path,
    private val permissionChecker: PermissionChecker<UUID>,
    private val luckPerms: LuckPerms?
) : PrefixesPlatform {

    override fun getDataDirectory(): File = path.toFile()

    override fun getPermissionChecker(): PermissionChecker<UUID> = permissionChecker

    override fun getLuckPerms(): LuckPerms? = luckPerms ?: runCatching { LuckPermsProvider.get() }.getOrNull()

    override fun getPlayerName(id: UUID): String {
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id)?.username ?: id.toString()
    }
}
