package manager

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import config.GlobalInstance.config as gC

@Serializable
data class AppParameters(
    val isDarkTheme: Boolean = false,
    val locale: String = "fr"
)

@Serializable
data class AppState(
    val isLoggedIn: Boolean = false,
    val lastSync: String = ""
)

@Serializable
data class AppData(
    val parameters: AppParameters = AppParameters(),
    val state: AppState = AppState()
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
                parameters = AppParameters(
                    isDarkTheme = gC.isDarkTheme.value,
                    locale = "fr"
                ),
                state = AppState(
                    isLoggedIn = false,
                    lastSync = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
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

    fun updateTheme(isDarkTheme: Boolean) {
        try {
            val currentData = loadAppData()
            val updatedParameters = currentData.parameters.copy(isDarkTheme = isDarkTheme)
            val updatedData = currentData.copy(parameters = updatedParameters)
            val jsonData = json.encodeToString(updatedData)
            File(APP_DATA_FILENAME).writeText(jsonData)
        }
        catch (e: Exception) {println("Error updating isDarkTheme: ${e.message}")}
    }

    fun updateLoginState(isLoggedIn: Boolean) {
        try {
            val currentData = loadAppData()
            val updatedState = currentData.state.copy(isLoggedIn = isLoggedIn)
            val updatedData = currentData.copy(state = updatedState)
            val jsonData = json.encodeToString(updatedData)
            File(APP_DATA_FILENAME).writeText(jsonData)
        }
        catch (e: Exception) {println("Error updating login state: ${e.message}")}
    }
}