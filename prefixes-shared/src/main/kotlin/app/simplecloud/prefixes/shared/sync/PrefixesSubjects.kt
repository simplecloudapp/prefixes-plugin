package app.simplecloud.prefixes.shared.sync

import app.simplecloud.api.runtime.SimpleCloudRuntime
import app.simplecloud.prefixes.shared.config.ALL_SYNC_SOURCE
import app.simplecloud.prefixes.shared.config.CURRENT_SYNC_SOURCE

class PrefixesSubjects(
    networkId: String,
    private val origin: String = SimpleCloudRuntime.groupName().ifBlank { SimpleCloudRuntime.serverId() },
    serverId: String = SimpleCloudRuntime.serverId()
) {

    private val root = "$networkId.prefixes"
    private val prefix = "$root.$origin.$serverId."

    fun own(subject: String): String = prefix + subject

    fun isOwn(subject: String): Boolean = subject.startsWith(prefix)

    fun publisherId(subject: String, event: String): String = subject.removeSuffix(".$event")

    fun patterns(sources: List<String>, subject: String): List<String> {
        val resolvedSources = sources
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .toList()

        if (ALL_SYNC_SOURCE in resolvedSources) {
            return listOf("$root.*.*.$subject")
        }

        return resolvedSources
            .map { source -> if (source == CURRENT_SYNC_SOURCE) origin else source }
            .distinct()
            .map { source -> "$root.$source.*.$subject" }
    }

    companion object {
        const val CHAT = "chat"
        const val TABLIST_UPDATE = "tablist.update"
        const val TABLIST_REMOVE = "tablist.remove"
        const val TABLIST_REQUEST = "tablist.request"
    }
}