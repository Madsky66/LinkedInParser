package config

import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import data.ProspectData
import manager.FileExportManager
import manager.FileImportManager
import manager.UrlManager
import utils.Colors
import utils.ConsoleMessage
import utils.ConsoleMessageType

data class GlobalConfig(
    var windowState: MutableState<WindowState> = mutableStateOf(WindowState(WindowPlacement.Floating, isMinimized = false, WindowPosition.PlatformDefault, DpSize(1280.dp, 720.dp))),
    var isWindowMaximized: MutableState<Boolean> = mutableStateOf(false),

    var drawerState: MutableState<DrawerState> = mutableStateOf(DrawerState(DrawerValue.Closed)),
    var isExpandedMenuItem: MutableState<String> = mutableStateOf(""),
    val drawerWidth: MutableState<Float> = mutableStateOf(if (isExpandedMenuItem.value != "") 0.8f else 0.2f),

    var isDarkTheme: MutableState<Boolean> = mutableStateOf(true),
    val themeColors: Colors = Colors(),
    var darkGray: MutableState<Color> = mutableStateOf<Color>(themeColors.get(isDarkTheme)[0]),
    var middleGray: MutableState<Color> = mutableStateOf<Color>(themeColors.get(isDarkTheme)[1]),
    var lightGray: MutableState<Color> =mutableStateOf<Color>(themeColors.get(isDarkTheme)[2]),

    val urlManager: UrlManager = UrlManager(),
    val fileImportManager: FileImportManager = FileImportManager(),
    val fileExportManager: FileExportManager = FileExportManager(),

    var showExportModal: MutableState<Boolean> = mutableStateOf(false),
    var showImportModal: MutableState<Boolean> = mutableStateOf(false),

    var filePath: MutableState<String> = mutableStateOf(""),
    var fileName: MutableState<String> = mutableStateOf(""),
    var fileFormat: MutableState<String> = mutableStateOf(""),

    var isApolloValidationLoading: MutableState<Boolean> = mutableStateOf(false),
    var isExtractionLoading: MutableState<Boolean> = mutableStateOf(false),
    var isImportationLoading: MutableState<Boolean> = mutableStateOf(false),
    var isExportationLoading: MutableState<Boolean> = mutableStateOf(false),

    var currentProfile: MutableState<ProspectData?> = mutableStateOf<ProspectData?>(null),
    var consoleMessage: MutableState<ConsoleMessage> = mutableStateOf(ConsoleMessage("En attente de données...", ConsoleMessageType.INFO)),

    var apiKey: MutableState<String> = mutableStateOf(""),
    var pastedUrl: MutableState<String> = mutableStateOf(""),
    var pastedInput: MutableState<String> = mutableStateOf(""),
    var selectedOptions: MutableList<Boolean> = mutableStateListOf(false, false),
)

object GlobalInstance {val config = GlobalConfig()}