package app.simplecloud.plugin.prefixes.api

interface PrefixesGroupIndexer<Player> {
    fun indexGroups(api: PrefixesApi<Player>)
}