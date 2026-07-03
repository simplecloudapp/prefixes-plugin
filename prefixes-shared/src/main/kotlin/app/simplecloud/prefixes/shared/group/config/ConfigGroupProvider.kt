package app.simplecloud.prefixes.shared.group.config

import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ConfigGroupProvider(
    private val config: () -> PrefixesConfig,
    private val permissionChecker: PermissionChecker<UUID>
) : GroupProvider {

    override val name: String = "Config"

    override fun getGroups(): Collection<PrefixesGroup> {
        val config = config()
        return config.groups
            .map { ConfigPrefixesGroup(it, permissionChecker) }
            .sortedByDescending { it.priority }
    }

    override fun getGroup(id: UUID): CompletableFuture<PrefixesGroup?> {
        return CompletableFuture.supplyAsync {
            val groups = getGroups()
            val config = config()

            // Check if player has permission for any group, highest priority first.
            val group = groups.firstOrNull {
                it.name != config.general.defaultGroup && it.containsPlayer(id)
            }

            // Fallback to the default group if no group matched.
            group ?: groups.firstOrNull { it.name == config.general.defaultGroup }
        }
    }
}