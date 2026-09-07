package app.simplecloud.prefixes.paper.platform

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import app.simplecloud.prefixes.shared.platform.PrefixesLogger
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.UUID

class PaperPlatformImpl(private val plugin: Plugin) : PrefixesPlatform {

    override fun getLogger(): PrefixesLogger {
        return PaperPrefixesLogger(plugin)
    }

    override fun getDataDirectory(): File {
        return plugin.dataFolder
    }

    override fun getPermissionChecker(): PermissionChecker<UUID> {
        return PermissionChecker { id, permission ->
            Bukkit.getPlayer(id)?.hasPermission(permission) ?: false
        }
    }

    override fun getLuckPerms(): LuckPerms? {
        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) return null
        return Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider
    }

    override fun getPlayerName(id: UUID): String {
        return Bukkit.getOfflinePlayer(id).name ?: id.toString()
    }
}
