package app.simplecloud.prefixes.shared.group.luckperms

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.shared.utilities.ColorParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.group.Group
import java.util.UUID
import java.util.concurrent.CompletableFuture

class LuckPermsGroup(
    private val group: Group,
    private val luckPerms: LuckPerms,
) : PrefixesGroup {

    override val name: String = group.name
    override val priority: Int = group.weight.orElse(0)
    override val permission: String = "group.${group.name}"
    override val prefix: Component? = miniMessage.deserialize(group.cachedData.metaData.prefix ?: "")
    override val suffix: Component? = miniMessage.deserialize(group.cachedData.metaData.suffix ?: "")
    override val color: TextColor? = parseColor()
    override val displayName: String = getMetaValue("display-name") ?: "<color><playername>"
    override val chatFormat: String = getMetaValue("chat-format") ?: "<prefix><color><playername><suffix> <#475569>» <#F8FAFC><message>"

    override fun containsPlayer(id: UUID): Boolean {
        return containsPlayerAsync(id).join()
    }

    override fun containsPlayerAsync(id: UUID): CompletableFuture<Boolean> {
        return luckPerms.userManager.loadUser(id).thenApply { user ->
            user.primaryGroup == name || user.getInheritedGroups(user.queryOptions).any { it.name == name }
        }
    }

    private fun parseColor(): TextColor? {
        val value = getMetaValue("color") ?: return TextColor.fromHexString("#FFFFFF")
        return ColorParser.parse(value) ?: TextColor.fromHexString("#FFFFFF")
    }

    private fun getMetaValue(key: String): String? {
        return group.cachedData.metaData.getMetaValue(key)
    }
}
