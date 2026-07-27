package app.simplecloud.prefixes.shared.utilities

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
}
