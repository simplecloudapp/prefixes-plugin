package app.simplecloud.prefixes.shared.command

import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.utilities.PrefixesPermissions
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import java.util.logging.Level
import java.util.logging.Logger

class PrefixesCommand<C : PrefixesSender>(
    private val manager: CommandManager<C>,
    private val prefixes: Prefixes
) {

    private val logger = Logger.getLogger(PrefixesCommand::class.java.name)

    private val messages get() = prefixes.messages.get()

    fun register() {
        registerRoot()
        registerHelp()
        registerReload()
    }

    private fun registerRoot() {
        manager.command(
            manager.commandBuilder("scprefix")
                .handler {
                    sendHelp(it.sender())
                }
        )
    }

    private fun registerHelp() {
        manager.command(
            manager.commandBuilder("scprefix")
                .literal("help")
                .handler {
                    sendHelp(it.sender())
                }
        )
    }

    private fun registerReload() {
        manager.command(
            manager.commandBuilder("scprefix")
                .literal("reload")
                .permission(PrefixesPermissions.RELOAD)
                .suspendingHandler {
                    val sender = it.sender()
                    if (!sender.hasPermission(PrefixesPermissions.RELOAD)) {
                        sender.sendMessage(messages.msg(messages.command.permission.denied))
                        return@suspendingHandler
                    }

                    runCatching {
                        prefixes.reload()
                    }.onSuccess {
                        sender.sendMessage(messages.msg(messages.command.reload.success))
                    }.onFailure { throwable ->
                        logger.log(Level.SEVERE, "Failed to reload SimpleCloud Prefixes", throwable)
                        sender.sendMessage(messages.msg(messages.command.reload.failed))
                    }
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