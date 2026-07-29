package app.simplecloud.prefixes.shared.utilities

import app.simplecloud.prefixes.shared.config.ALL_SYNC_SOURCE
import app.simplecloud.prefixes.shared.config.CURRENT_SYNC_SOURCE
import kotlin.test.Test
import kotlin.test.assertEquals

class PrefixesSubjectsTest {

    private val subjects = PrefixesSubjects(
        networkId = "network",
        origin = "lobby",
        serverId = "lobby-1"
    )

    @Test
    fun `extracts the same publisher from update and remove subjects`() {
        val updateSubject = subjects.own(PrefixesSubjects.TABLIST_UPDATE)
        val removeSubject = subjects.own(PrefixesSubjects.TABLIST_REMOVE)

        assertEquals(
            "network.prefixes.lobby.lobby-1",
            subjects.publisherId(updateSubject, PrefixesSubjects.TABLIST_UPDATE)
        )
        assertEquals(
            "network.prefixes.lobby.lobby-1",
            subjects.publisherId(removeSubject, PrefixesSubjects.TABLIST_REMOVE)
        )
    }

    @Test
    fun `resolves the current server group`() {
        assertEquals(
            listOf("network.prefixes.lobby.*.chat"),
            subjects.patterns(listOf(CURRENT_SYNC_SOURCE), PrefixesSubjects.CHAT)
        )
    }

    @Test
    fun `accepts group names and persistent server ids in the same source list`() {
        assertEquals(
            listOf(
                "network.prefixes.survival.*.chat",
                "network.prefixes.standalone-lobby-1.*.chat"
            ),
            subjects.patterns(
                listOf("survival", "standalone-lobby-1"),
                PrefixesSubjects.CHAT
            )
        )
    }

    @Test
    fun `resolves all servers to one wildcard pattern`() {
        assertEquals(
            listOf("network.prefixes.*.*.tablist.update"),
            subjects.patterns(listOf(ALL_SYNC_SOURCE), PrefixesSubjects.TABLIST_UPDATE)
        )
    }

    @Test
    fun `ignores blank and duplicate sources`() {
        assertEquals(
            listOf("network.prefixes.lobby.*.chat"),
            subjects.patterns(
                listOf("", " lobby ", CURRENT_SYNC_SOURCE, CURRENT_SYNC_SOURCE),
                PrefixesSubjects.CHAT
            )
        )
    }
}
