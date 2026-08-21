package app.simplecloud.prefixes.shared.group.luckperms

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.shared.utilities.ColorParser
import net.kyori.adventure.text.Component
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.group.Group
import net.luckperms.api.model.user.User
import net.luckperms.api.node.types.MetaNode
import net.luckperms.api.node.types.PrefixNode
import net.luckperms.api.node.types.SuffixNode
import net.luckperms.api.node.types.WeightNode
import java.util.UUID
import java.util.concurrent.CompletableFuture

class LuckPermsGroupProvider(
    private val luckPerms: LuckPerms
) : GroupProvider {

    override fun getName(): String = "luckperms"

    override fun getGroups(): CompletableFuture<Collection<PrefixesGroup>> {
        val groups = luckPerms.groupManager.loadedGroups
            .map { LuckPermsGroup(it, luckPerms) }
            .sortedByDescending { it.priority }

        return CompletableFuture.completedFuture(groups)
    }

    override fun getGroup(id: UUID): CompletableFuture<PrefixesGroup?> {
        val user = luckPerms.userManager.getUser(id)
        if (user != null) {
            return CompletableFuture.completedFuture(resolveGroup(user))
        }

        return luckPerms.userManager.loadUser(id).thenApply(::resolveGroup)
    }

    override fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean> {
        return luckPerms.groupManager.loadGroup(group.name).thenCompose { existing ->
            if (existing.isPresent) {
                return@thenCompose CompletableFuture.completedFuture(false)
            }

            luckPerms.groupManager.createAndLoadGroup(group.name).thenCompose { created ->
                applyPrefixes(created, group)
                luckPerms.groupManager.saveGroup(created).thenApply { true }
            }
        }
    }

    private fun applyPrefixes(target: Group, source: PrefixesGroup) {
        val data = target.data()
        data.add(WeightNode.builder(source.priority).build())

        val prefix = miniMessage.serialize(source.prefix ?: Component.empty())
        if (prefix.isNotEmpty()) {
            data.add(PrefixNode.builder(prefix, source.priority).build())
        }

        val suffix = miniMessage.serialize(source.suffix ?: Component.empty())
        if (suffix.isNotEmpty()) {
            data.add(SuffixNode.builder(suffix, source.priority).build())
        }

        val color = ColorParser.serialize(source.color)
        if (color.isNotEmpty()) {
            data.add(MetaNode.builder("color", color).build())
        }

        data.add(MetaNode.builder("display-name", source.displayName).build())
        data.add(MetaNode.builder("chat-format", source.chatFormat).build())
    }

    private fun resolveGroup(user: User): PrefixesGroup? {
        val primaryGroupName = user.primaryGroup
        val inheritedGroups = user.getInheritedGroups(user.queryOptions)
        val primaryGroup = luckPerms.groupManager.getGroup(primaryGroupName)
        val candidates = if (primaryGroup == null) inheritedGroups else inheritedGroups + primaryGroup

        val group = selectHighestWeightedGroup(candidates, primaryGroupName) ?: return null
        return LuckPermsGroup(group, luckPerms)
    }

    private fun selectHighestWeightedGroup(groups: Iterable<Group>, primaryGroupName: String): Group? {
        return groups
            .distinctBy { it.name }
            .sortedWith(
                compareByDescending<Group> { it.weight.orElse(0) }
                    .thenByDescending { it.name == primaryGroupName }
                    .thenBy { it.name }
            )
            .firstOrNull()
    }
}
