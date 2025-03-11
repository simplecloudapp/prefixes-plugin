package app.simplecloud.plugin.prefixes.api

import app.simplecloud.plugin.prefixes.api.impl.PrefixesApiImpl

interface PrefixesPluginLoader {

    /**
     * Instantiates a new PrefixesApi (useful for sharding)
     * @return a [PrefixesApiImpl] (null if load failed)
     */
    fun load(): PrefixesApiImpl?
}