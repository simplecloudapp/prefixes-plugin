package app.simplecloud.prefixes.shared.group.luckperms

import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import net.luckperms.api.LuckPerms
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
}