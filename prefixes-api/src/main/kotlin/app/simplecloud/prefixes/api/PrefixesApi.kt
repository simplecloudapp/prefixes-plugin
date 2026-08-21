package app.simplecloud.prefixes.api

import app.simplecloud.prefixes.api.group.GroupProvider
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
    fun getGroups(): CompletableFuture<Collection<PrefixesGroup>>

    /**
     * Returns the highest-priority applicable [PrefixesGroup] of a player.
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

    /**
     * Registers a group provider under its [GroupProvider.name].
     *
     * @param provider The provider to register
     */
    fun registerGroupProvider(provider: GroupProvider)

    /**
     * Removes a registered group provider.
     *
     * @param name Name of the provider to remove
     */
    fun unregisterGroupProvider(name: String)

    /**
     * Returns the provider currently used.
     */
    fun getGroupProvider(): GroupProvider

    /**
     * Returns all registered group providers.
     */
    fun getGroupProviders(): Collection<GroupProvider>

    /**
     * Reapplies the prefix data of a player on this server.
     *
     * @param id Target player UUID
     */
    fun refreshPlayer(id: UUID)

    /**
     * Reapplies the prefix data of all players on this server.
     */
    fun refreshAll()
}
