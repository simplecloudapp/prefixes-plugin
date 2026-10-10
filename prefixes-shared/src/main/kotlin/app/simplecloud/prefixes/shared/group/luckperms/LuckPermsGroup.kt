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
import net.luckperms.api.model.user.User
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
    override val displayName: String = getMetaValue("display-name") ?: ""
    override val chatFormat: String = getMetaValue("chat-format") ?: ""

    override fun containsPlayer(id: UUID): CompletableFuture<Boolean> {
        val user = luckPerms.userManager.getUser(id)
        if (user != null) {
            return CompletableFuture.completedFuture(isMember(user))
        }

        return PrefixesConstants.SCOPE.future { isMember(luckPerms.userManager.loadUser(id).await()) }
    }

    private fun isMember(user: User): Boolean =
        user.primaryGroup == name || user.getInheritedGroups(user.queryOptions).any { it.name == name }

    private fun parseColor(): TextColor {
        val value = getMetaValue("color") ?: return NamedTextColor.WHITE
        return ColorParser.parse(value) ?: NamedTextColor.WHITE
    }

    private fun getMetaValue(key: String): String? {
        return group.cachedData.metaData.getMetaValue(key)
    }
}
