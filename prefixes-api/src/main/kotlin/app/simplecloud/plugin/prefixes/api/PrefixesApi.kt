package app.simplecloud.plugin.prefixes.api

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.*

interface PrefixesApi {

    /**
     * Registers a player to be able to see prefixes
     * @param uniqueId UUID of the target player
     */
    fun registerViewer(uniqueId: UUID)

    /**
     * Returns if a viewer exists
     * @param uniqueId UUID of the target player
     */
    fun hasViewer(uniqueId: UUID): Boolean

    /**
     * Removes a viewer
     * @param uniqueId UUID of the target player
     */
    fun removeViewer(uniqueId: UUID)

    /**
     * Sets the prefix and suffix of a player in both Tab and Chat
     * @param uniqueId UUID of the target player
     * @param group
     * @param viewers A list of all viewers of this change (if empty, everyone is affected)
     */
    fun setWholeName(uniqueId: UUID, group: PrefixesGroup, vararg viewers: UUID)

    /**
     * Sets the prefix and suffix of a player in both Tab and Chat
     * @param uniqueId UUID of the target player
     * @param groupName
     * @param viewers A list of all viewers of this change (if empty, everyone is affected)
     */
    fun setWholeName(uniqueId: UUID, groupName: String, vararg viewers: UUID)

    /**
     * Sets the prefix and suffix of a player in both Tab and Chat
     * @param uniqueId UUID of the target player
     * @param prefix the targets prefix
     * @param color the targets team color
     * @param suffix the targets suffix
     * @param priority the users Tablist priority
     * @param viewers A list of all viewers of this change (if empty, everyone is affected)
     */
    fun setWholeName(uniqueId: UUID, prefix: Component, color: TextColor, suffix: Component, priority: Int, vararg viewers: UUID)

    /**
     * Sets the prefix of a player in both Tab and Chat
     * @param uniqueId UUID of the target player
     * @param prefix prefix to set
     * @param viewers A list of all viewers of this change (if empty, everyone is affected)
     */
    fun setPrefix(uniqueId: UUID, prefix: Component, vararg viewers: UUID)

    /**
     * Sets the prefix of a player in both Tab and Chat
     * @param uniqueId UUID of the target player
     * @param suffix suffix to set
     * @param viewers A list of all viewers of this change (if empty, everyone is affected)
     */
    fun setSuffix(uniqueId: UUID, suffix: Component, vararg viewers: UUID)

    /**
     * Returns all registered [PrefixesGroup] ordered by priority
     */
    fun getGroups(): List<PrefixesGroup>

    /**
     * Returns the highest [PrefixesGroup] of a player
     * @param uniqueId UUID of the target player
     */
    fun getHighestGroup(uniqueId: UUID): PrefixesGroup

    /**
     * Adds a [PrefixesGroup]
     * @param group
     */
    fun addGroup(group: PrefixesGroup)

    /**
     * Changes the [PrefixesActor] of the server instance (e.g. to a bukkit actor)
     * @param actor
     */
    fun setActor(actor: PrefixesActor)

    /**
     * Changes the Scoreboard Team color of the target player (Used in 1.12+ to make player names colorful)
     * @param uniqueId UUID of the target player
     * @param color the [TextColor] of the target players team
     * @param viewers A list of all viewers of this change (if empty, everyone is affected)
     */
    fun setColor(uniqueId: UUID, color: TextColor, vararg viewers: UUID)

    /**
     * Sets the used PrefixesConfig
     * @param config Specifies the new [PrefixesConfig]
     */
    fun setConfig(config: PrefixesConfig)

    /**
     * Returns a formatted chat message of the target player that will be sent to the viewer
     * @param target UUID of the target player
     * @param viewer UUID of the viewing player (if null, only default prefix and suffix of the players group will be shown)
     * @param format the chat format the message should follow
     * @param message Message sent by the [target]
     */
    fun formatChatMessage(target: UUID, viewer: UUID?, format: String, message: Component): Component
    /**
     * Sets the prefix and suffix of a player in both Tab and Chat
     * @param uniqueId UUID of the target player
     * @param group
     * @param viewers A list of all viewers of this change (if empty, everyone is affected)
     */
    fun setWholeName(uniqueId: UUID, group: PrefixesGroup, viewers: Audience)

    /**
     * Sets the prefix and suffix of a player in both Tab and Chat
     * @param uniqueId UUID of the target player
     * @param groupName
     * @param viewers An [Audience] of all viewers of this change (if empty, everyone is affected)
     */
    fun setWholeName(uniqueId: UUID, groupName: String, viewers: Audience)

    /**
     * Sets the prefix and suffix of a player in both Tab and Chat
     * @param uniqueId UUID of the target player
     * @param prefix the targets prefix
     * @param color the targets team color
     * @param suffix the targets suffix
     * @param viewers An [Audience] of all viewers of this change (if empty, everyone is affected)
     */
    fun setWholeName(uniqueId: UUID, prefix: Component, color: TextColor, suffix: Component, priority: Int, viewers: Audience)
    /**
     * Sets the prefix of a player in both Tab and Chat
     * @param uniqueId UUID of the target player
     * @param prefix prefix to set
     * @param viewers A list of all viewers of this change (if empty, everyone is affected)
     */
    fun setPrefix(uniqueId: UUID, prefix: Component, viewers: Audience)

    /**
     * Sets the prefix of a player in both Tab and Chat
     * @param uniqueId UUID of the target player
     * @param suffix suffix to set
     * @param viewers An [Audience] of all viewers of this change (if empty, everyone is affected)
     */
    fun setSuffix(uniqueId: UUID, suffix: Component, viewers: Audience)

    /**
     * Changes the Scoreboard Team color of the target player (Used in 1.12+ to make player names colorful)
     * @param uniqueId UUID of the target player
     * @param color [TextColor] the color of the target players team
     * @param viewers An [Audience] of all viewers of this change (if empty, everyone is affected)
     */
    fun setColor(uniqueId: UUID, color: TextColor, viewers: Audience)

    /**
     * Returns a formatted chat message of the target player that will be sent to the viewer
     * @param target UUID of the target player
     * @param viewer An [Audience] of the viewing player (if empty, only default prefix and suffix of the targets group will be shown)
     * @param format the chat format the message should follow
     * @param message Message sent by the [target]
     */
    fun formatChatMessage(target: UUID, viewer: Audience, format: String, message: Component): Component

}