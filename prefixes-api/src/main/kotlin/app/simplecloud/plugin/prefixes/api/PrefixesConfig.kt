package app.simplecloud.plugin.prefixes.api

class PrefixesConfig {
    private var chatFormat: String = "<prefix><name_colored><suffix><gray>:</gray> <white><message></white>"

    fun getChatFormat(): String {
        return chatFormat
    }
}