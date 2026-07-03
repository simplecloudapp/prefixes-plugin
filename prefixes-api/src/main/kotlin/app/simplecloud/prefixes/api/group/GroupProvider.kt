package app.simplecloud.prefixes.api.group

import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Represents a source provider for groups.
 */
interface GroupProvider {

    /**
     * The unique identifier name of this provider.
     */
    val name: String

    /**
     * Returns all registered prefix groups.
     */
    fun getGroups(): Collection<PrefixesGroup>

    /**
     * Gets the primary group of a player.
     *
     * @param id The player's UUID
     * @return A future completing with the player's group, or null if none matches.
     */
    fun getGroup(id: UUID): CompletableFuture<PrefixesGroup?>
}