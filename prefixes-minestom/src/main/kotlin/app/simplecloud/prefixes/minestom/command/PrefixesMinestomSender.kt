package app.simplecloud.prefixes.minestom.command

import app.simplecloud.prefixes.minestom.permission.MinestomPermissions
import app.simplecloud.prefixes.shared.command.PrefixesSender
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender

class PrefixesMinestomSender(
    val sender: CommandSender,
    private val permissions: MinestomPermissions
) : PrefixesSender {

    override fun sendMessage(message: Component) = sender.sendMessage(message)

    override fun hasPermission(permission: String): Boolean = permissions.hasPermission(sender, permission)

}
