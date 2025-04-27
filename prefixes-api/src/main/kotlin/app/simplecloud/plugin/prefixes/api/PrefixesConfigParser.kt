package app.simplecloud.plugin.prefixes.api

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileReader

class PrefixesConfigParser(private val configFile: File) {
    fun parse(type: Class<PrefixesConfig>, default: PrefixesConfig): PrefixesConfig {
        if (!configFile.exists()) return default
        val gson = Gson()
        val reader = JsonReader(FileReader(configFile))
        return gson.fromJson(reader, type)
    }
}