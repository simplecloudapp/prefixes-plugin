package app.simplecloud.prefixes.shared.group.luckperms

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.shared.utilities.ColorParser
import app.simplecloud.prefixes.shared.PrefixesConstants
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.group.Group
import java.util.UUID
import java.util.concurrent.CompletableFuture

class LuckPermsGroup(
    private val group: Group,
    private val luckPerms: LuckPerms
) : PrefixesGroup {

    override val name: String = group.name
    override val priority: Int = group.weight.orElse(0)
    override val permission: String = "group.${group.name}"
    override val prefix: Component = miniMessage.deserialize(group.cachedData.metaData.prefix ?: "")
    override val suffix: Component = miniMessage.deserialize(group.cachedData.metaData.suffix ?: "")
    override val color: TextColor = parseColor()
    override val displayName: String = getMetaValue("display-name") ?: PrefixesConstants.DEFAULT_DISPLAY_NAME
    override val chatFormat: String = getMetaValue("chat-format") ?: PrefixesConstants.DEFAULT_CHAT_FORMAT

    override fun containsPlayer(id: UUID): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            val user = luckPerms.userManager.loadUser(id).await()
            user.primaryGroup == name || user.getInheritedGroups(user.queryOptions).any { it.name == name }
        }
    }

    private fun parseColor(): TextColor {
        val value = getMetaValue("color") ?: return NamedTextColor.WHITE
        return ColorParser.parse(value) ?: NamedTextColor.WHITE
    }

    private fun getMetaValue(key: String): String? {
        return group.cachedData.metaData.getMetaValue(key)
    }
}
