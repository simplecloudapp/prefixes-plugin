package app.simplecloud.prefixes.shared.group.luckperms

import app.simplecloud.prefixes.api.PrefixesApi
import net.luckperms.api.LuckPerms
import net.luckperms.api.event.EventSubscription
import net.luckperms.api.event.node.NodeMutateEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.luckperms.api.model.group.Group
import net.luckperms.api.model.user.User

class LuckPermsListener(
    private val luckPerms: LuckPerms,
    private val api: PrefixesApi
) {

    fun register(): List<EventSubscription<*>> = listOf(
        luckPerms.eventBus.subscribe(UserDataRecalculateEvent::class.java) { event -> api.refreshPlayer(event.user.uniqueId) },
        luckPerms.eventBus.subscribe(NodeMutateEvent::class.java, ::onNodeMutate)
    )

    private fun onNodeMutate(event: NodeMutateEvent) {
        when (val target = event.target) {
            is User -> api.refreshPlayer(target.uniqueId)
            is Group -> api.refreshAll()
        }
    }
}
