package app.simplecloud.prefixes.api.group

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Represents a prefixes group.
 */
interface PrefixesGroup {

    /** The name of this group. */
    val name: String

    /** The priority of this group. */
    val priority: Int

    /** The permission required for this group, or an empty string if none. */
    val permission: String

    /** The prefix of this group. */
    val prefix: Component

    /** The suffix of this group. */
    val suffix: Component

    /** The name color of this group. */
    val color: TextColor

    /** The display name of this group. */
    val displayName: String

    /** The chat format of this group. */
    val chatFormat: String

    /** Returns whether the player [id] is a member of this group. */
    fun containsPlayer(id: UUID): CompletableFuture<Boolean>
}
