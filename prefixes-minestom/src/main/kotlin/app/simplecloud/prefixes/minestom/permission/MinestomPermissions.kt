package app.simplecloud.prefixes.minestom.permission

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.entity.Player
import java.util.UUID
import java.util.function.BiPredicate

class MinestomPermissions(
    private val groupPermission: BiPredicate<Player, String>?,
    private val commandPermission: BiPredicate<Player, String>?
) {

    val hasGroupPermission: Boolean = groupPermission != null

    fun hasPermission(sender: CommandSender, permission: String): Boolean {
        if (permission.isEmpty()) return true
        if (sender is ConsoleSender) return true
        if (sender !is Player) return false

        if (commandPermission != null) return commandPermission.test(sender, permission)
        return sender.permissionLevel >= 4
    }

    fun getChecker(): PermissionChecker<UUID> = PermissionChecker { id, permission ->
        if (groupPermission == null) return@PermissionChecker false
        val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) ?: return@PermissionChecker false

        groupPermission.test(player, permission)
    }

}
