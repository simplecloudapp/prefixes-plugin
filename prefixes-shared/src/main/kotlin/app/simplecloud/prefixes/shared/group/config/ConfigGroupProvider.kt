package app.simplecloud.prefixes.shared.group.config

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.shared.config.CONFIG_SOURCE
import app.simplecloud.prefixes.shared.config.ConfigGroup
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.utilities.ColorParser
import net.kyori.adventure.text.Component
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ConfigGroupProvider(
    private val configFactory: ConfigurationFactory<PrefixesConfig>,
    private val permissionChecker: PermissionChecker<UUID>
) : GroupProvider {

    override val name: String = CONFIG_SOURCE

    override fun getGroups(): CompletableFuture<Collection<PrefixesGroup>> {
        return CompletableFuture.completedFuture(loadGroups())
    }

    override fun getGroup(id: UUID): CompletableFuture<PrefixesGroup?> {
        return CompletableFuture.supplyAsync {
            val groups = loadGroups()
            val config = configFactory.get()

            // Check if player has permission for any group, highest priority first.
            val group = groups.firstOrNull {
                it.name != config.general.defaultGroup && it.hasPermission(id)
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

                configFactory.save(config.copy(groups = config.groups + createConfigGroup(group)))
                true
            }
        }
    }

    private fun loadGroups(): List<ConfigPrefixesGroup> {
        return configFactory.get().groups
            .map { ConfigPrefixesGroup(it, permissionChecker) }
            .sortedByDescending { it.priority }
    }

    private fun createConfigGroup(group: PrefixesGroup) = ConfigGroup(
        name = group.name,
        priority = group.priority,
        permission = group.permission,
        prefix = miniMessage.serialize(group.prefix ?: Component.empty()),
        suffix = miniMessage.serialize(group.suffix ?: Component.empty()),
        color = ColorParser.serialize(group.color),
        displayName = group.displayName,
        chatFormat = group.chatFormat
    )
}
