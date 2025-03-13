package app.simplecloud.plugin.prefixes.api

import app.simplecloud.plugin.prefixes.api.impl.PrefixesApiImpl
import net.luckperms.api.LuckPerms

class PrefixesApiLuckPermsImpl(private var luckPerms: LuckPerms) : PrefixesApiImpl() {
    override fun indexGroups() {
        getGroups().clear()
        luckPerms.groupManager.loadAllGroups().newIncompleteFuture<Unit>().completeAsync {
            luckPerms.groupManager.loadedGroups.forEach {
                addGroup(LuckPermsGroup(it, luckPerms))
            }
        }
    }
}