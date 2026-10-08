package app.simplecloud.prefixes.api

import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Entrypoint to interacting with prefixes.
 */
interface PrefixesApi {

    /** Returns all registered groups ordered by priority. */
    fun getGroups(): CompletableFuture<Collection<PrefixesGroup>>

    /** Returns the primary group of the player [id], or null if none matches. */
    fun getPrimaryGroup(id: UUID): CompletableFuture<PrefixesGroup?>

    /** Returns the prefix data everyone sees for the player [id]. */
    fun getPrefixData(id: UUID): CompletableFuture<PrefixesPlayerData>

    /** Returns the prefix data [viewer] sees for the player [id]. */
    fun getPrefixData(id: UUID, viewer: Audience): CompletableFuture<PrefixesPlayerData>

    /** Sets the [prefix] of the player [id] for everyone. */
    fun setPrefix(id: UUID, prefix: Component)

    /** Sets the [prefix] of the player [id] for [viewers]. */
    fun setPrefix(id: UUID, prefix: Component, viewers: Audience)

    /** Sets the [suffix] of the player [id] for everyone. */
    fun setSuffix(id: UUID, suffix: Component)

    /** Sets the [suffix] of the player [id] for [viewers]. */
    fun setSuffix(id: UUID, suffix: Component, viewers: Audience)

    /** Sets the name [color] of the player [id] for everyone. */
    fun setColor(id: UUID, color: TextColor)

    /** Sets the name [color] of the player [id] for [viewers]. */
    fun setColor(id: UUID, color: TextColor, viewers: Audience)

    /** Sets the tablist [priority] of the player [id] for everyone, higher is listed further up. */
    fun setPriority(id: UUID, priority: Int)

    /** Sets the tablist [priority] of the player [id] for [viewers]. */
    fun setPriority(id: UUID, priority: Int, viewers: Audience)

    /** Shows the player [id] with the group named [group] for everyone, completes with `false` if it does not exist. */
    fun setGroup(id: UUID, group: String): CompletableFuture<Boolean>

    /** Shows the player [id] with the group named [group] to [viewers], completes with `false` if it does not exist. */
    fun setGroup(id: UUID, group: String, viewers: Audience): CompletableFuture<Boolean>

    /** Shows all players in [targets] with the group named [group] for everyone, completes with `false` if it does not exist. */
    fun setGroup(targets: Audience, group: String): CompletableFuture<Boolean>

    /** Shows all players in [targets] with the group named [group] to [viewers], completes with `false` if it does not exist. */
    fun setGroup(targets: Audience, group: String, viewers: Audience): CompletableFuture<Boolean>

    /** Shows the player [id] with [group] for everyone. */
    fun setGroup(id: UUID, group: PrefixesGroup)

    /** Shows the player [id] with [group] to [viewers]. */
    fun setGroup(id: UUID, group: PrefixesGroup, viewers: Audience)

    /** Shows all players in [targets] with [group] for everyone. */
    fun setGroup(targets: Audience, group: PrefixesGroup)

    /** Shows all players in [targets] with [group] to [viewers]. */
    fun setGroup(targets: Audience, group: PrefixesGroup, viewers: Audience)

    /** Removes all manually set values of the player [id]. */
    fun reset(id: UUID)

    /** Removes all manually set values of the players in [targets]. */
    fun reset(targets: Audience)

    /** Removes the values set for [viewers] from the players in [targets]. */
    fun reset(targets: Audience, viewers: Audience)

    /** Adds [group] to the current group provider, completes with `false` if it already exists. */
    fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean>

    /** Registers [provider] under its name. */
    fun registerGroupProvider(provider: GroupProvider)

    /** Removes the group provider named [name]. */
    fun unregisterGroupProvider(name: String)

    /** Returns the group provider currently used. */
    fun getGroupProvider(): GroupProvider

    /** Returns all registered group providers. */
    fun getGroupProviders(): Collection<GroupProvider>

    /** Reloads the group of the player [id], manually set values stay. */
    fun refreshPlayer(id: UUID)

    /** Reloads the groups of all players on this server, manually set values stay. */
    fun refreshAll()
}
