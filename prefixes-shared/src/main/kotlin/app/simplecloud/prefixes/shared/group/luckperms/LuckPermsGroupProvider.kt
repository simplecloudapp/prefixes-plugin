package app.simplecloud.prefixes.shared.group.luckperms

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.group.Group
import net.luckperms.api.node.types.MetaNode
import net.luckperms.api.node.types.PrefixNode
import net.luckperms.api.node.types.SuffixNode
import net.luckperms.api.node.types.WeightNode
import java.util.UUID
import java.util.concurrent.CompletableFuture

class LuckPermsGroupProvider(
    private val luckPerms: LuckPerms
) : GroupProvider {

    override val name: String = "LuckPerms"

    override fun getGroups(): Collection<PrefixesGroup> {
        return luckPerms.groupManager.loadedGroups
            .map { LuckPermsGroup(it, luckPerms) }
            .sortedByDescending { it.priority }
    }

    override fun getGroup(id: UUID): CompletableFuture<PrefixesGroup?> {
        val user = luckPerms.userManager.getUser(id)
        if (user != null) {
            val primaryGroup = user.primaryGroup
            val group = luckPerms.groupManager.getGroup(primaryGroup)
            return CompletableFuture.completedFuture(group?.let { LuckPermsGroup(it, luckPerms) })
        }

        return luckPerms.userManager.loadUser(id).thenApply { loadedUser ->
            val primaryGroup = loadedUser.primaryGroup
            val group = luckPerms.groupManager.getGroup(primaryGroup) ?: return@thenApply null
            LuckPermsGroup(group, luckPerms)
        }
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
        target.data().add(WeightNode.builder(source.priority).build())

        source.prefix?.let { miniMessage.serialize(it) }?.takeIf { it.isNotEmpty() }?.let {
            target.data().add(PrefixNode.builder(it, source.priority).build())
        }
        source.suffix?.let { miniMessage.serialize(it) }?.takeIf { it.isNotEmpty() }?.let {
            target.data().add(SuffixNode.builder(it, source.priority).build())
        }
        source.color?.let { "<${it.asHexString().uppercase()}>" }?.let {
            target.data().add(MetaNode.builder("color", it).build())
        }

        target.data().add(MetaNode.builder("display-name", source.displayName).build())
        target.data().add(MetaNode.builder("chat-format", source.chatFormat).build())
    }
}
