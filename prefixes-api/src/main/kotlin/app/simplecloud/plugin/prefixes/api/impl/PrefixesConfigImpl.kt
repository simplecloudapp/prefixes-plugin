package app.simplecloud.plugin.prefixes.api.impl

import app.simplecloud.plugin.prefixes.api.PrefixesConfig

class PrefixesConfigImpl : PrefixesConfig {

    private var chatFormat: String = "<prefix><name_colored><suffix><gray>:</gray> <white><message></white>"

    override fun getChatFormat(): String {
        return chatFormat
    }
}