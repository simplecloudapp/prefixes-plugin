package app.simplecloud.prefixes.shared.group.config

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.shared.config.ConfigGroup
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ConfigGroupProvider(
    private val configFactory: ConfigurationFactory<PrefixesConfig>,
    private val permissionChecker: PermissionChecker<UUID>
) : GroupProvider {

    override val name: String = "Config"

    override fun getGroups(): Collection<PrefixesGroup> {
        val config = configFactory.get()
        return config.groups
            .map { ConfigPrefixesGroup(it, permissionChecker) }
            .sortedByDescending { it.priority }
    }

    override fun getGroup(id: UUID): CompletableFuture<PrefixesGroup?> {
        return CompletableFuture.supplyAsync {
            val groups = getGroups()
            val config = configFactory.get()

            // Check if player has permission for any group, highest priority first.
            val group = groups.firstOrNull {
                it.name != config.general.defaultGroup && it.containsPlayer(id)
            }

            // Fallback to the default group if no group matched.
            group ?: groups.firstOrNull { it.name == config.general.defaultGroup }
        }
    }

    override fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            synchronized(this) {
                val config = configFactory.get()
                if (config.groups.any { it.name.equals(group.name, ignoreCase = true) }) {
                    return@synchronized false
                }

                configFactory.save(config.copy(groups = config.groups + group.toConfigGroup()))
                true
            }
        }
    }

    private fun PrefixesGroup.toConfigGroup() = ConfigGroup(
        name = name,
        priority = priority,
        permission = permission,
        prefix = prefix?.let { miniMessage.serialize(it) } ?: "",
        suffix = suffix?.let { miniMessage.serialize(it) } ?: "",
        color = color?.let { "<${it.asHexString().uppercase()}>" } ?: "",
        displayName = displayName,
        chatFormat = chatFormat
    )
}
