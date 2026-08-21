package app.simplecloud.prefixes.shared.group.config

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.plugin.api.shared.permission.PermissionChecker
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.shared.config.ConfigGroup
import app.simplecloud.prefixes.shared.utilities.ColorParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ConfigPrefixesGroup(
    private val group: ConfigGroup,
    private val permissionChecker: PermissionChecker<UUID>
) : PrefixesGroup {
    override val name: String = group.name
    override val priority: Int = group.priority
    override val permission: String = group.permission
    override val prefix: Component? = miniMessage.deserialize(group.prefix)
    override val suffix: Component? = miniMessage.deserialize(group.suffix)
    override val color: TextColor? = ColorParser.parse(group.color)
    override val displayName: String = group.displayName
    override val chatFormat: String = group.chatFormat

    override fun containsPlayer(id: UUID): Boolean {
        if (permission.isEmpty()) return true // Default group matches everyone
        return permissionChecker.checkPermission(id, permission)
    }

    override fun containsPlayerAsync(id: UUID): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync { containsPlayer(id) }
    }

}