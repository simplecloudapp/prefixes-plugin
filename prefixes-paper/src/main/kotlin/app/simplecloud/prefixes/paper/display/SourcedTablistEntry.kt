package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry

internal data class SourcedTablistEntry(
    val publisherId: String,
    val entry: TablistEntry
)
