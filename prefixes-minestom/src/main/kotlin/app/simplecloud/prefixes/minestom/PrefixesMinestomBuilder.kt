package app.simplecloud.prefixes.minestom

import net.luckperms.api.LuckPerms
import net.minestom.server.entity.Player
import java.nio.file.Path
import java.util.function.BiPredicate

/**
 * Builds a [PrefixesMinestom] instance.
 */
class PrefixesMinestomBuilder internal constructor(private val directory: Path) {

    private var luckPerms: LuckPerms? = null
    private var groupPermission: BiPredicate<Player, String>? = null
    private var registerCommands = true
    private var commandPermission: BiPredicate<Player, String>? = null

    /**
     * Sets the LuckPerms instance used when the group source is `luckperms`.
     */
    fun luckPerms(luckPerms: LuckPerms): PrefixesMinestomBuilder {
        this.luckPerms = luckPerms
        return this
    }

    /**
     * Sets the check that decides which group a player gets when the group source is `config`.
     */
    fun groupPermission(check: BiPredicate<Player, String>): PrefixesMinestomBuilder {
        this.groupPermission = check
        return this
    }

    /**
     * Sets whether the prefixes commands are registered.
     */
    fun registerCommands(enabled: Boolean): PrefixesMinestomBuilder {
        this.registerCommands = enabled
        return this
    }

    /**
     * Sets the check that decides whether a player may use a prefixes command.
     * The console is always allowed. Without a check, only players with permission level 4 are allowed.
     */
    fun commandPermission(check: BiPredicate<Player, String>): PrefixesMinestomBuilder {
        this.commandPermission = check
        return this
    }

    /**
     * Creates the instance and starts prefixes.
     */
    fun enable(): PrefixesMinestom {
        return PrefixesMinestom(directory, luckPerms, groupPermission, registerCommands, commandPermission).init()
    }
}
