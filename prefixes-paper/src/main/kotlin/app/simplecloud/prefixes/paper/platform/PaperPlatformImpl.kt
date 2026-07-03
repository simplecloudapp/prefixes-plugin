package app.simplecloud.prefixes.paper.platform

import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.UUID

class PaperPlatformImpl(plugin: Plugin) : PrefixesPlatform {

    override val dataDirectory: File = plugin.dataFolder

    override val permissionChecker: PermissionChecker<UUID> = PermissionChecker { id, permission ->
        Bukkit.getPlayer(id)?.hasPermission(permission) ?: false
    }

    override val playerResolver: (UUID) -> String = { id ->
        Bukkit.getOfflinePlayer(id).name ?: id.toString()
    }

    override val luckPerms: LuckPerms?
        get() = if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider
        } else {
            null
        }
}