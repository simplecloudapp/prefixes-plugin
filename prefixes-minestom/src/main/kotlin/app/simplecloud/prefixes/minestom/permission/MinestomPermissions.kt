package app.simplecloud.prefixes.minestom.permission

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.entity.Player
import java.util.UUID
import java.util.function.BiPredicate

class MinestomPermissions(private val handler: BiPredicate<CommandSender, String>?) {

    val hasHandler: Boolean = handler != null

    fun hasPermission(sender: CommandSender, permission: String): Boolean {
        if (permission.isEmpty()) return true
        if (sender is ConsoleSender) return true
        if (sender is Player && sender.permissionLevel >= 4) return true

        return handler?.test(sender, permission) ?: false
    }

    fun getChecker(): PermissionChecker<UUID> = PermissionChecker { id, permission ->
        val handler = handler ?: return@PermissionChecker false
        val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) ?: return@PermissionChecker false

        handler.test(player, permission)
    }

}
