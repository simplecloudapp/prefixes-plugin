package app.simplecloud.plugin.prefixes.api

interface PrefixesDisplay<P> {
    /**
     * This is called whenever we transition from another display (global display)
     */
    fun transition(player: P, content: PrefixesPlayerData)

    fun apply(player: P, content: PrefixesPlayerData)
    fun getCurrent(player: P): PrefixesPlayerData?
    fun remove(player: P)
    fun getAll(): Map<P, PrefixesPlayerData>
}