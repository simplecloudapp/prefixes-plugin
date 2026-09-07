package app.simplecloud.prefixes.shared.platform

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import net.luckperms.api.LuckPerms
import java.io.File
import java.util.UUID

/**
 * Represents a platform on which prefixes runs.
 */
interface PrefixesPlatform {

    /**
     * Returns this platform's logger.
     */
    fun getLogger(): PrefixesLogger

    /**
     * Returns this platform's data directory.
     */
    fun getDataDirectory(): File

    /**
     * Returns this platform's permission checker.
     */
    fun getPermissionChecker(): PermissionChecker<UUID>

    /**
     * Returns the LuckPerms instance on the platform, or null if not available.
     */
    fun getLuckPerms(): LuckPerms?

    /**
     * Returns the name of a player.
     */
    fun getPlayerName(id: UUID): String
}
