package app.simplecloud.prefixes.paper

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.incendo.cloud.bukkit.parser.ItemStackParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CloudItemStackParserTest {
    @Test
    fun `item parser initializes against Paper 26_3`() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()

        assertNotNull(ItemStackParser.itemStackParser<Any>())
        val parser = Class.forName("org.incendo.cloud.bukkit.parser.ItemStackParser\$ModernParser")
        val field = parser.getDeclaredField("AS_BUKKIT_STACK_METHOD")
        field.isAccessible = true
        val method = field.get(null) as java.lang.reflect.Method
        assertEquals("asBukkitMirror", method.name)
    }
}
