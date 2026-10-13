package app.simplecloud.prefixes.shared.command

import app.simplecloud.plugin.api.shared.command.CloudSender
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.utilities.PrefixesPermissions
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.incendo.cloud.CommandManager

class PrefixesCommand<C : CloudSender>(
    private val manager: CommandManager<C>,
    private val prefixes: Prefixes
) {

    private val messages get() = prefixes.messages.get()

    fun register() {
        registerHelp()
        registerReload()
    }

    private fun registerHelp() {
        val root = manager.commandBuilder("scprefix")
        manager.command(root.handler { sendHelp(it.sender()) })
        manager.command(root.literal("help").handler { sendHelp(it.sender()) })
    }

    private fun registerReload() {
        manager.command(
            manager.commandBuilder("scprefix")
                .literal("reload")
                .permission(PrefixesPermissions.RELOAD)
                .handler {
                    val message = if (prefixes.reload()) messages.command.reload.success else messages.command.reload.failed
                    it.sender().sendMessage(messages.msg(message))
                }
        )
    }

    private fun sendHelp(sender: C) {
        if (!sender.hasPermission(PrefixesPermissions.COMMAND)) {
            sender.sendMessage(messages.msg(messages.command.permission.denied))
            return
        }

        sender.sendMessage(messages.msg(messages.command.help.title))
        sender.sendMessage(messages.msg(messages.command.help.entry, Placeholder.unparsed("command", "/scprefix help")))

        if (sender.hasPermission(PrefixesPermissions.RELOAD)) {
            sender.sendMessage(messages.msg(messages.command.help.entry, Placeholder.unparsed("command", "/scprefix reload")))
        }
    }
}
