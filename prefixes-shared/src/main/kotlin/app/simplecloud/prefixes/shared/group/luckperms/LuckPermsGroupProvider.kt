package app.simplecloud.prefixes.shared.group.luckperms

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.shared.utilities.ColorParser
import app.simplecloud.prefixes.shared.PrefixesConstants
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
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

    private val logger = PrefixesConstants.LOGGER

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

        return PrefixesConstants.SCOPE.future { loadGroup(id) }
    }

    override fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            val existing = luckPerms.groupManager.loadGroup(group.name).await()
            if (existing.isPresent) return@future false

            val created = luckPerms.groupManager.createAndLoadGroup(group.name).await()
            applyPrefixes(created, group)
            luckPerms.groupManager.saveGroup(created).await()
            true
        }
    }

    private suspend fun loadGroup(id: UUID): PrefixesGroup? {
        return try {
            resolveGroup(luckPerms.userManager.loadUser(id).await())
        } catch (exception: Exception) {
            logger.error("Failed to load LuckPerms user $id", exception)
            null
        }
    }

    private fun applyPrefixes(target: Group, source: PrefixesGroup) {
        val data = target.data()
        data.add(WeightNode.builder(source.priority).build())

        val prefix = miniMessage.serialize(source.prefix)
        if (prefix.isNotEmpty()) {
            data.add(PrefixNode.builder(prefix, source.priority).build())
        }

        val suffix = miniMessage.serialize(source.suffix)
        if (suffix.isNotEmpty()) {
            data.add(SuffixNode.builder(suffix, source.priority).build())
        }

        data.add(MetaNode.builder("color", ColorParser.serialize(source.color)).build())

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
