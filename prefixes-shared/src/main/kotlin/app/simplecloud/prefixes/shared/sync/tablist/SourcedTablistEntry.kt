package app.simplecloud.prefixes.shared.sync.tablist

import kotlin.time.TimeMark

data class SourcedTablistEntry(
    val publisherId: String,
    val entry: TablistEntry,
    val updatedAt: TimeMark
)