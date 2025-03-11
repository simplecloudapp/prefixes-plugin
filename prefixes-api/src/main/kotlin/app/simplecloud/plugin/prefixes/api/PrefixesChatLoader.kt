package app.simplecloud.plugin.prefixes.api

import app.simplecloud.plugin.prefixes.api.impl.PrefixesApiImpl

interface PrefixesChatLoader {

    /**
     * Instantiates the chat provider of PrefixesApi (useful for sharding)
     * @param api the [PrefixesApiImpl] object the ChatLoader belongs to
     */
    fun load(api: PrefixesApiImpl)
}