package app.simplecloud.prefixes.paper.command

import app.simplecloud.prefixes.shared.command.PrefixesSender
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component

class PrefixesPaperSender(
    val stack: CommandSourceStack
) : PrefixesSender {

    override fun sendMessage(message: Component) = stack.sender.sendMessage(message)

    override fun hasPermission(permission: String): Boolean = stack.sender.hasPermission(permission)

}