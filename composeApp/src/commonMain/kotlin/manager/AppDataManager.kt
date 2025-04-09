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
    val locale: String = "fr",
    val googleSheetsId: String = "",
    val apiKey: String = "",
    val lastExportPath: String = "",
    val lastExportFileName: String = ""
)

@Serializable
data class AppState(
    val isLoggedIn: Boolean = false,
    val lastSync: String = "",
    val selectedExportOptions: List<Boolean> = listOf(false, false)
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
                    locale = "fr",
                    googleSheetsId = gC.googleSheetsId.value,
                    apiKey = gC.apiKey.value,
                    lastExportPath = gC.filePath.value,
                    lastExportFileName = gC.fileName.value
                ),
                state = AppState(
                    isLoggedIn = false,
                    lastSync = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                    selectedExportOptions = gC.selectedOptions.toList()
                )
            )
            val jsonData = json.encodeToString(AppData.serializer(), appData)
            File(APP_DATA_FILENAME).writeText(jsonData)
        }
        catch (e: Exception) {println("Error saving app data: ${e.message}")}
    }

    fun loadAppData(): AppData {
        val file = File(APP_DATA_FILENAME)
        return try {
            if (file.exists()) {
                val jsonData = file.readText()
                json.decodeFromString(AppData.serializer(), jsonData)
            }
            else {AppData()}
        }
        catch (e: Exception) {
            println("Error loading app data: ${e.message}")
            AppData()
        }
    }

    fun applyAppData() {
        val appData = loadAppData()
        gC.isDarkTheme.value = appData.parameters.isDarkTheme
        gC.googleSheetsId.value = appData.parameters.googleSheetsId
        gC.apiKey.value = appData.parameters.apiKey
        gC.filePath.value = appData.parameters.lastExportPath
        gC.fileName.value = appData.parameters.lastExportFileName
        if (appData.state.selectedExportOptions.isNotEmpty()) {for (i in appData.state.selectedExportOptions.indices) {if (i < gC.selectedOptions.size) {gC.selectedOptions[i] = appData.state.selectedExportOptions[i]}}}
        updateThemeColors()
    }

    fun updateTheme(isDarkTheme: Boolean) {
        try {
            gC.isDarkTheme.value = isDarkTheme
            updateThemeColors()
            saveAppData()
        }
        catch (e: Exception) {println("Error updating isDarkTheme: ${e.message}")}
    }

    private fun updateThemeColors() {
        val colors = gC.themeColors.get(gC.isDarkTheme)
        gC.darkGray.value = colors[0]
        gC.middleGray.value = colors[1]
        gC.lightGray.value = colors[2]
    }
}