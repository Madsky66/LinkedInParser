package config

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogState
import data.ProspectData
import manager.FileExportManager
import manager.FileImportManager
import manager.LinkedInManager
import manager.UrlManager
import utils.Colors
import utils.ConsoleMessage
import utils.ConsoleMessageType
import java.io.File

data class GlobalConfig(
    var isExpandedMenuItem: MutableState<String> = mutableStateOf(""),

    var isDarkTheme: MutableState<Boolean> = mutableStateOf(true),
    val themeColors: Colors = Colors(),
    var darkGray: MutableState<Color> = mutableStateOf<Color>(themeColors.get(isDarkTheme)[0]),
    var middleGray: MutableState<Color> = mutableStateOf<Color>(themeColors.get(isDarkTheme)[1]),
    var lightGray: MutableState<Color> =mutableStateOf<Color>(themeColors.get(isDarkTheme)[2]),

    val urlManager: UrlManager = UrlManager(),
    val linkedinManager: LinkedInManager = LinkedInManager(),
    val fileImportManager: FileImportManager = FileImportManager(),
    val fileExportManager: FileExportManager = FileExportManager(),

    var dialogState: MutableState<DialogState> = mutableStateOf(DialogState()),
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

    var currentProfile: MutableState<ProspectData?> = mutableStateOf(null),
    var consoleMessage: MutableState<ConsoleMessage> = mutableStateOf(ConsoleMessage("En attente de données...", ConsoleMessageType.INFO)),

    var apiKey: MutableState<String> = mutableStateOf(""),
    var pastedApiKey: MutableState<String> = mutableStateOf(""),
    var pastedUrl: MutableState<String> = mutableStateOf(""),
    var pastedInput: MutableState<String> = mutableStateOf(""),
    var selectedOptions: MutableList<Boolean> = mutableStateListOf(false, false),
)

object GlobalInstance {val config = GlobalConfig()}