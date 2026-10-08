package app.simplecloud.prefixes.minestom.permission

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.entity.Player
import java.util.UUID
import java.util.function.BiPredicate

class MinestomPermissions(private val check: BiPredicate<Player, String>?) {

    val hasCheck: Boolean = check != null

    fun hasPermission(sender: CommandSender, permission: String): Boolean {
        if (permission.isEmpty()) return true
        if (sender is ConsoleSender) return true
        if (sender !is Player) return false

        if (check != null) return check.test(sender, permission)
        return sender.permissionLevel >= 4
    }

    fun getChecker(): PermissionChecker<UUID> = PermissionChecker { id, permission ->
        if (check == null) return@PermissionChecker false
        val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) ?: return@PermissionChecker false

        check.test(player, permission)
    }

}
