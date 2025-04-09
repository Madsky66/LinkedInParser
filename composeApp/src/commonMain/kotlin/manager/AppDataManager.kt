package manager

import config.GlobalInstance.config as gC
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class AppData(
    val parameters: Map<String, Any> = emptyMap(),
    val state: Map<String, String> = emptyMap()
)

object AppDataManager {
    private const val APP_DATA_FILENAME = "app_data.json"
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun saveAppData() {
        try {
            val appData = AppData(
                parameters = mapOf(
                    "theme" to gC.isDarkTheme.value,
                    "locale" to "fr"
                ),
                state = mapOf(
                    "isLoggedIn" to "false",
                    "lastSync" to LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                )
            )
            val jsonData = json.encodeToString(appData)
            File(APP_DATA_FILENAME).writeText(jsonData)
        }
        catch (e: Exception) {println("Error saving app data: ${e.message}")}
    }

    fun loadAppData(): AppData {
        val file = File(APP_DATA_FILENAME)
        return try {
            if (file.exists()) {
                val jsonData = file.readText()
                json.decodeFromString(jsonData)
            }
            else {AppData()}
        }
        catch (e: Exception) {
            println("Error loading app data: ${e.message}")
            AppData()
        }
    }

    fun updateParameter(key: String, value: Any) {
        try {
            val currentData = loadAppData()
            val updatedParameters = currentData.parameters.toMutableMap()
            updatedParameters[key] = value
            val updatedData = currentData.copy(parameters = updatedParameters)
            val jsonData = json.encodeToString(updatedData)
            File(APP_DATA_FILENAME).writeText(jsonData)
        }
        catch (e: Exception) {println("Error updating parameter: ${e.message}")}
    }

    fun updateState(key: String, value: String) {
        try {
            val currentData = loadAppData()
            val updatedState = currentData.state.toMutableMap()
            updatedState[key] = value
            val updatedData = currentData.copy(state = updatedState)
            val jsonData = json.encodeToString(updatedData)
            File(APP_DATA_FILENAME).writeText(jsonData)
        }
        catch (e: Exception) {println("Error updating state: ${e.message}")}
    }
}