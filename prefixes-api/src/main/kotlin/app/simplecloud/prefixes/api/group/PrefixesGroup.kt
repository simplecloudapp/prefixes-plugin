package app.simplecloud.prefixes.api.group

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * A group that defines how its players are shown.
 */
interface PrefixesGroup {

    /** The name of this group. */
    val name: String

    /** The priority of this group, higher wins and is listed further up. */
    val priority: Int

    /** The permission required for this group, or an empty string if none. */
    val permission: String

    /** The prefix of this group. */
    val prefix: Component

    /** The suffix of this group. */
    val suffix: Component

    /** The name color of this group. */
    val color: TextColor

    /** The MiniMessage format of the display name. */
    val displayName: String

    /** The MiniMessage format of chat messages. */
    val chatFormat: String

    /** Returns whether the player [id] is a member of this group. */
    fun containsPlayer(id: UUID): CompletableFuture<Boolean>
}
