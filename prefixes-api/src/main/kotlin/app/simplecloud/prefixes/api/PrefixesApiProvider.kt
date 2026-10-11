package app.simplecloud.prefixes.api

import org.jetbrains.annotations.ApiStatus

/**
 * Provides access to the [PrefixesApi] instance.
 */
object PrefixesApiProvider {

    @Volatile
    private var instance: PrefixesApi? = null

    /**
     * Returns the [PrefixesApi] instance.
     *
     * @throws IllegalStateException if prefixes is not loaded yet
     */
    @JvmStatic
    fun get(): PrefixesApi {
        return instance ?: throw IllegalStateException("PrefixesApi is not loaded yet!")
    }

    @ApiStatus.Internal
    @JvmStatic
    fun register(api: PrefixesApi) {
        instance = api
    }

    @ApiStatus.Internal
    @JvmStatic
    fun unregister() {
        instance = null
    }
}
