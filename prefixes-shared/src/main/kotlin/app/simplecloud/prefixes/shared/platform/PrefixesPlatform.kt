package app.simplecloud.prefixes.shared.platform

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import net.luckperms.api.LuckPerms
import java.io.File
import java.util.UUID

/**
 * Represents the platform environment on which the plugin runs.
 */
interface PrefixesPlatform {

    /**
     * The platform-specific plugin data directory.
     */
    val dataDirectory: File

    /**
     * The platform-specific permission checker.
     */
    val permissionChecker: PermissionChecker<UUID>

    /**
     * The platform-specific player name resolver.
     */
    val playerResolver: (UUID) -> String

    /**
     * The LuckPerms instance on the platform, or null if not available.
     */
    val luckPerms: LuckPerms?
}
