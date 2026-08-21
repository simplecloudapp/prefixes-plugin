package app.simplecloud.prefixes.api.group

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Represents a prefixes group.
 */
interface PrefixesGroup {

    /**
     * The name of this group.
     */
    val name: String

    /**
     * The priority of this group (higher priority values override lower ones).
     */
    val priority: Int

    /**
     * The permission required for this group, or an empty string if none.
     */
    val permission: String

    /**
     * The prefix of this group.
     */
    val prefix: Component?

    /**
     * The suffix of this group.
     */
    val suffix: Component?

    /**
     * The primary color of this group.
     */
    val color: TextColor?

    /**
     * The display name format of this group.
     */
    val displayName: String

    /**
     * The chat format of this group.
     */
    val chatFormat: String

    /**
     * Checks whether the specified player is a member of this group.
     *
     * @param id the UUID of the player
     * @return a future completing with `true` if the player belongs to this group,
     * otherwise `false`
     */
    fun containsPlayer(id: UUID): CompletableFuture<Boolean>
}