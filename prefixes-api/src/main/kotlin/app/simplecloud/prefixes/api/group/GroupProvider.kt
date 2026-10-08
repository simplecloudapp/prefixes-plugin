package app.simplecloud.prefixes.api.group

import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * A source provider of prefix groups, e.g. the config or LuckPerms.
 */
interface GroupProvider {

    /** Returns the name of this provider. */
    fun getName(): String

    /** Returns all groups of this provider. */
    fun getGroups(): CompletableFuture<Collection<PrefixesGroup>>

    /** Returns the primary group of the player [id], or null if none matches. */
    fun getGroup(id: UUID): CompletableFuture<PrefixesGroup?>

    /** Adds [group] to this provider, completes with `false` if it already exists. */
    fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean>
}
