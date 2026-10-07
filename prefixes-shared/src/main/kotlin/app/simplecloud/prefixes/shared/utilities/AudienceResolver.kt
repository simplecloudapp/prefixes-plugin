package app.simplecloud.prefixes.shared.utilities

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import java.util.UUID

object AudienceResolver {

    fun resolveIds(audience: Audience): List<UUID> = buildList {
        audience.forEachAudience { member -> member.get(Identity.UUID).ifPresent(::add) }
    }

    fun resolveId(audience: Audience): UUID? = audience.get(Identity.UUID).orElse(null)
}
