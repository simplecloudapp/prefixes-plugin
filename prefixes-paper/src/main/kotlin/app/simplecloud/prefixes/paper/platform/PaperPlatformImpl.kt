package app.simplecloud.prefixes.paper.platform

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.UUID

class PaperPlatformImpl(private val plugin: Plugin) : PrefixesPlatform {

    private val permissionChecker: PermissionChecker<UUID> = PermissionChecker { id, permission ->
        Bukkit.getPlayer(id)?.hasPermission(permission) ?: false
    }

    override fun getDataDirectory(): File = plugin.dataFolder

    override fun getPermissionChecker(): PermissionChecker<UUID> = permissionChecker

    override fun getLuckPerms(): LuckPerms? {
        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) return null
        return Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider
    }

    override fun getPlayerName(id: UUID): String {
        return Bukkit.getOfflinePlayer(id).name ?: id.toString()
    }
}
