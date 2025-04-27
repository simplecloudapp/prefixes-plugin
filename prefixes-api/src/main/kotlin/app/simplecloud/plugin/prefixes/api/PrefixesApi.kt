package app.simplecloud.plugin.prefixes.api

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.*

interface PrefixesApi<Player> {

    fun registerAudience(audience: Audience)
    fun hasAudience(audience: Audience): Boolean
    fun removeAudience(audience: Audience)

    /**
     * Sets the prefix and suffix of a player in both Tab and Chat
     * @param player Target player
     * @param group
     * @param audience An audience to this change (if null, everyone is affected)
     */
    fun setWholeName(player: Player, group: PrefixesGroup, audience: Audience? = null)

    /**
     * Sets the prefix and suffix of a player in both Tab and Chat
     * @param player Target player
     * @param groupName
     * @param audience An audience to this change (if null, everyone is affected)
     */
    fun setWholeName(player: Player, groupName: String, audience: Audience? = null)

    /**
     * Sets the prefix and suffix of a player in both Tab and Chat
     * @param player Target player
     * @param prefix the target prefix
     * @param color the target team color
     * @param suffix the target suffix
     * @param priority the users Tablist priority
     * @param audience An audience to this change (if null, everyone is affected)
     */
    fun setWholeName(
        player: Player,
        prefix: Component,
        color: TextColor,
        suffix: Component,
        priority: Int,
        audience: Audience? = null
    )

    fun setWholeName(player: Player, data: PrefixesPlayerData, audience: Audience? = null)

    fun editWholeName(player: Player, audience: Audience? = null, action: (PrefixesPlayerData) -> Unit)

    /**
     * Sets the prefix of a player in both Tab and Chat
     * @param player Target player
     * @param prefix prefix to set
     * @param audience An audience to this change (if null, everyone is affected)
     */
    fun setPrefix(player: Player, prefix: Component, audience: Audience? = null)

    /**
     * Sets the prefix of a player in both Tab and Chat
     * @param player Target player
     * @param suffix suffix to set
     * @param audience An audience to this change (if null, everyone is affected)
     */
    fun setSuffix(player: Player, suffix: Component, audience: Audience? = null)

    /**
     * Returns all registered [PrefixesGroup] ordered by priority
     */
    fun getGroups(): List<PrefixesGroup>

    /**
     * Returns the highest [PrefixesGroup] of a player
     * @param player Target player
     */
    fun getHighestGroup(player: Player): PrefixesGroup

    /**
     * Adds a [PrefixesGroup]
     * @param group
     */
    fun addGroup(group: PrefixesGroup)

    /**
     * Changes the Scoreboard Team color of the target player (Used in 1.12+ to make player names colorful)
     * @param player Target player
     * @param color the [TextColor] of the target players team
     * @param audience An audience to this change (if null, everyone is affected)
     */
    fun setColor(player: Player, color: TextColor, audience: Audience? = null)

    /**
     * Sets the used PrefixesConfig
     * @param config Specifies the new [PrefixesConfig]
     */
    fun setConfig(config: PrefixesConfig)


    /**
     * Returns a formatted chat message of the target player that will be sent to the viewer
     * @param target Target player
     * @param audience An audience to this change (if null, everyone is affected)
     * @param format the chat format the message should follow
     * @param message Message sent by the [target]
     */
    fun formatChatMessage(target: Player, format: String, message: Component, audience: Audience? = null): Component

}