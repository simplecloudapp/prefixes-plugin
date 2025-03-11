package app.simplecloud.plugin.prefixes.shared

import app.simplecloud.plugin.prefixes.api.PrefixesGroup
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.group.Group
import java.util.*
import java.util.concurrent.CompletableFuture

class LuckPermsGroup(private var group: Group, private var luckPerms: LuckPerms) : PrefixesGroup {

    override fun getName(): String {
        return group.name
    }

    override fun getPrefix(): Component? {
        return group.cachedData.metaData.prefix?.let { MiniMessageImpl.parse(it) }
    }

    override fun getColor(): TextColor? {
        return TextColor.fromHexString(group.cachedData.metaData.getMetaValue("color") ?: "#FFFFFF")
    }

    override fun getSuffix(): Component? {
        return group.cachedData.metaData.suffix?.let { MiniMessageImpl.parse(it) }
    }

    override fun getPriority(): Int {
        return group.weight.orElse(0)
    }

    override fun containsPlayer(uniqueId: UUID): Boolean {
        return containsPlayerFuture(uniqueId).join()
    }

    override fun containsPlayerFuture(uniqueId: UUID): CompletableFuture<Boolean> {
        return luckPerms.userManager.loadUser(uniqueId).thenApplyAsync { user ->
            return@thenApplyAsync user.primaryGroup == getName()
        }
    }
}