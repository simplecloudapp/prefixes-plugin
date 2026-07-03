package app.simplecloud.prefixes.shared.utilities.config

import app.simplecloud.prefixes.shared.config.ConfigGroup

object DefaultConfigs {

    val VARIABLES: Map<String, String> = mapOf("prefix" to "<color:#38bdf8><bold>⚡</bold></color>")

    val GROUPS: List<ConfigGroup> = listOf(
        ConfigGroup(
            name = "owner",
            priority = 100,
            permission = "simplecloud.prefix.group.owner",
            prefix = "<#DC2626><bold>Owner</bold> <#475569>| ",
            suffix = "",
            color = "<#DC2626>",
            displayName = "<color><playername>",
            chatFormat = "<prefix><color><playername><suffix> <#475569>» <#F8FAFC><message>"
        ),
        ConfigGroup(
            name = "admin",
            priority = 90,
            permission = "simplecloud.prefix.group.admin",
            prefix = "<#F59E0B><bold>Admin</bold> <#475569>| ",
            suffix = "",
            color = "<#F59E0B>",
            displayName = "<color><playername>",
            chatFormat = "<prefix><color><playername><suffix> <#475569>» <#F8FAFC><message>"
        ),
        ConfigGroup(
            name = "default",
            priority = 0,
            permission = "",
            prefix = "<#94A3B8>Player <#475569>| ",
            suffix = "",
            color = "<#94A3B8>",
            displayName = "<color><playername>",
            chatFormat = "<prefix><color><playername><suffix> <#475569>» <#F8FAFC><message>"
        )
    )

}