package app.simplecloud.prefixes.shared.group.luckperms

import net.luckperms.api.model.group.Group
import java.lang.reflect.Proxy
import java.util.OptionalInt
import kotlin.test.Test
import kotlin.test.assertEquals

class LuckPermsGroupProviderTest {

    @Test
    fun `selects the applicable group with the highest weight`() {
        val selected = selectHighestWeightedGroup(
            listOf(
                group("member", 10),
                group("admin", 100),
                group("moderator", 50)
            ),
            primaryGroupName = "member"
        )

        assertEquals("admin", selected?.name)
    }

    @Test
    fun `prefers the primary group when weights are equal`() {
        val selected = selectHighestWeightedGroup(
            listOf(
                group("admin", 100),
                group("moderator", 100)
            ),
            primaryGroupName = "moderator"
        )

        assertEquals("moderator", selected?.name)
    }

    @Test
    fun `uses the group name as a stable final tie breaker`() {
        val groups = listOf(
            group("moderator", 100),
            group("admin", 100)
        )

        val selected = selectHighestWeightedGroup(groups, primaryGroupName = "member")
        val selectedFromReversedInput = selectHighestWeightedGroup(groups.reversed(), primaryGroupName = "member")

        assertEquals("admin", selected?.name)
        assertEquals("admin", selectedFromReversedInput?.name)
    }

    @Test
    fun `treats a missing group weight as zero`() {
        val selected = selectHighestWeightedGroup(
            listOf(
                group("unweighted"),
                group("negative", -1)
            ),
            primaryGroupName = "negative"
        )

        assertEquals("unweighted", selected?.name)
    }

    private fun group(name: String, weight: Int? = null): Group {
        return Proxy.newProxyInstance(
            Group::class.java.classLoader,
            arrayOf(Group::class.java)
        ) { proxy, method, arguments ->
            when (method.name) {
                "getName" -> name
                "getWeight" -> weight?.let(OptionalInt::of) ?: OptionalInt.empty()
                "equals" -> proxy === arguments?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "Group(name=$name, weight=$weight)"
                else -> error("Unexpected Group method: ${method.name}")
            }
        } as Group
    }
}
