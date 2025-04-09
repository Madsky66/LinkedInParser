package manager

import config.GlobalInstance.config as gC
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class AppData(val parameters: Map<String, Any> = emptyMap(), val state: Map<String, String> = emptyMap())

object AppDataManager {
    private const val appDataFileName = "app_data.json"
    fun saveAppData() {
        val appData = AppData(
            mapOf("theme" to gC.isDarkTheme.value, "locale" to "fr"),
            mapOf("isLoggedIn" to "false", "lastSync" to "2025-04-07")
        )
        val jsonData = Json.encodeToString(appData)
        File(appDataFileName).writeText(jsonData)
    }
    fun loadAppData(): AppData {
        val file = File(appDataFileName)
        return if (file.exists()) {val jsonData = file.readText(); Json.decodeFromString(jsonData)} else {AppData()}
    }
}