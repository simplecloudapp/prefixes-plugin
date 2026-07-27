package app.simplecloud.prefixes.shared.utilities

import app.simplecloud.api.runtime.SimpleCloudRuntime
import app.simplecloud.prefixes.shared.config.SyncTargets

class PrefixesSubjects(
    networkId: String,
    origin: String = SimpleCloudRuntime.groupName().ifBlank { SimpleCloudRuntime.serverId() },
    serverId: String = SimpleCloudRuntime.serverId()
) {

    private val root = "$networkId.prefixes"
    private val prefix = "$root.$origin.$serverId."

    fun own(subject: String): String = prefix + subject

    fun isOwn(subject: String): Boolean = subject.startsWith(prefix)

    fun patterns(targets: SyncTargets, subject: String): List<String> {
        if (targets.allServers) return listOf("$root.*.*.$subject")
        return (targets.serverGroups + targets.persistentServers).map { "$root.$it.*.$subject" }
    }

    companion object {
        const val CHAT = "chat"
        const val TABLIST_UPDATE = "tablist.update"
        const val TABLIST_REMOVE = "tablist.remove"
        const val TABLIST_REQUEST = "tablist.request"
    }
}