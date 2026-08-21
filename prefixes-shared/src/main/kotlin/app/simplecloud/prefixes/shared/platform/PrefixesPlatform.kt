package app.simplecloud.prefixes.shared.platform

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import net.luckperms.api.LuckPerms
import java.io.File
import java.util.UUID

/**
 * Represents a platform on which the plugin runs.
 */
interface PrefixesPlatform {

    /**
     * Returns the platform-specific data directory.
     */
    fun getDataDirectory(): File

    /**
     * Returns the platform-specific permission checker.
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
