package app.simplecloud.prefixes.minestom

import net.luckperms.api.LuckPerms
import net.minestom.server.entity.Player
import java.nio.file.Path
import java.util.function.BiPredicate

/**
 * Builds a [PrefixesMinestom] instance.
 */
@ConsistentCopyVisibility
data class PrefixesMinestomBuilder internal constructor(
    private val directory: Path,
    private val permission: BiPredicate<Player, String>? = null,
    private val registerCommands: Boolean = true,
    private val luckPerms: LuckPerms? = null
) {

    /**
     * Sets the check that decides which group a player gets and whether they may use the prefixes commands.
     */
    fun permission(check: BiPredicate<Player, String>): PrefixesMinestomBuilder = copy(permission = check)

    /** Sets whether the prefixes commands are registered. */
    fun registerCommands(enabled: Boolean): PrefixesMinestomBuilder = copy(registerCommands = enabled)

    /** Sets the [LuckPerms] instance, only needed when it is not available through the LuckPerms provider. */
    fun luckPerms(luckPerms: LuckPerms): PrefixesMinestomBuilder = copy(luckPerms = luckPerms)

    /** Creates the instance and starts prefixes. */
    fun enable(): PrefixesMinestom = PrefixesMinestom.start(directory, permission, registerCommands, luckPerms)
}
