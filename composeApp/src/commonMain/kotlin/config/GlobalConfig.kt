package config

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import data.ProspectData
import manager.*
import utils.*
import java.io.File

data class GlobalConfig(
    var isExpandedMenuItem: MutableState<String> = mutableStateOf(""),

    var isDarkTheme: MutableState<Boolean> = mutableStateOf(true),
    val themeColors: Colors = Colors(),
    var darkGray: MutableState<Color> = mutableStateOf<Color>(Color(0xFF2A2A2A)),
    var middleGray: MutableState<Color> = mutableStateOf<Color>(Color.DarkGray),
    var lightGray: MutableState<Color> = mutableStateOf<Color>(Color.LightGray),

    val urlManager: UrlManager = UrlManager(),
    val linkedinManager: LinkedInManager = LinkedInManager(),
    val fileImportManager: FileImportManager = FileImportManager(),
    val fileExportManager: FileExportManager = FileExportManager(),

    var showConfirmModal: MutableState<Boolean> = mutableStateOf(false),
    var showExportModal: MutableState<Boolean> = mutableStateOf(false),
    var showImportModal: MutableState<Boolean> = mutableStateOf(false),
    var isWaitingForSelection: MutableState<Boolean> = mutableStateOf(false),

    var fileInstance: MutableState<File?> = mutableStateOf(null),
    var fileFullPath: MutableState<String> = mutableStateOf(""),
    var filePath: MutableState<String> = mutableStateOf(""),
    var fileName: MutableState<String> = mutableStateOf(""),
    var fileFormat: MutableState<String> = mutableStateOf(""),

    var isExtractionLoading: MutableState<Boolean> = mutableStateOf(false),
    var isImportationLoading: MutableState<Boolean> = mutableStateOf(false),
    var isExportationLoading: MutableState<Boolean> = mutableStateOf(false),

    var consoleMessage: MutableState<ConsoleMessage> = mutableStateOf(ConsoleMessage("En attente de données...", ConsoleMessageType.INFO)),
    var currentProfile: MutableState<ProspectData?> = mutableStateOf(null),
    var googleSheetsId: MutableState<String> = mutableStateOf(""),
    var apiKey: MutableState<String> = mutableStateOf(""),

    var pastedApiKey: MutableState<String> = mutableStateOf(""),
    var pastedUrl: MutableState<String> = mutableStateOf(""),
    var pastedInput: MutableState<String> = mutableStateOf(""),
    var selectedOptions: MutableList<Boolean> = mutableStateListOf(false, false),
) {
    fun updateThemeColors() {
        val colors = themeColors.get(isDarkTheme)
        darkGray.value = colors[0]
        middleGray.value = colors[1]
        lightGray.value = colors[2]
    }
}

object GlobalInstance {
    val config = GlobalConfig()
    init {config.updateThemeColors()}
}