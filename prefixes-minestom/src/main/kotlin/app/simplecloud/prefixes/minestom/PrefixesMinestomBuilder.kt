package app.simplecloud.prefixes.minestom

import net.luckperms.api.LuckPerms
import net.minestom.server.command.CommandSender
import java.nio.file.Path
import java.util.function.BiPredicate

/**
 * Builds a [PrefixesMinestom] instance.
 */
class PrefixesMinestomBuilder internal constructor(private val directory: Path) {

    private var permissionHandler: BiPredicate<CommandSender, String>? = null
    private var commands = true
    private var luckPerms: LuckPerms? = null

    /**
     * Sets the permission handler.
     */
    fun permissionHandler(handler: BiPredicate<CommandSender, String>): PrefixesMinestomBuilder = apply {
        this.permissionHandler = handler
    }

    /**
     * Registers the `/scprefix` command. Enabled by default.
     */
    fun commands(enabled: Boolean): PrefixesMinestomBuilder = apply {
        this.commands = enabled
    }

    /**
     * Sets the LuckPerms instance used when the group source is `luckperms`.
     */
    fun luckPerms(luckPerms: LuckPerms): PrefixesMinestomBuilder = apply {
        this.luckPerms = luckPerms
    }

    /**
     * Creates the instance and starts the plugin.
     */
    fun enable(): PrefixesMinestom {
        return PrefixesMinestom(directory, permissionHandler, commands, luckPerms).enable()
    }
}
