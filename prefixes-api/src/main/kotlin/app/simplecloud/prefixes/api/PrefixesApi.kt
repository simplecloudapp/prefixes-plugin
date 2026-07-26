package app.simplecloud.prefixes.api

import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Main entrypoint to interacting with prefixes.
 */
interface PrefixesApi {

    /**
     * Returns all registered [PrefixesGroup] ordered by priority.
     */
    fun getGroups(): Collection<PrefixesGroup>

    /**
     * Returns the primary [PrefixesGroup] of a player.
     * @param id Target player UUID
     */
    fun getPrimaryGroup(id: UUID): CompletableFuture<PrefixesGroup?>

    /**
     * Returns the prefix data for a player.
     * @param id Target player UUID
     */
    fun getPrefixData(id: UUID): CompletableFuture<PrefixesPlayerData>

    /**
     * Adds a new group to the group provider.
     *
     * @param group The group to add
     * @return A future completing with `true` if the group was created, or `false` if a group with that name already exists.
     */
    fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean>

}