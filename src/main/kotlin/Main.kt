import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.rememberDrawerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ui.composable.App
import ui.composable.DrawerMenuContent
import utils.Colors
import kotlin.system.exitProcess

fun main() = application {
    val applicationScope: CoroutineScope = rememberCoroutineScope()
    val windowState = rememberWindowState(WindowPlacement.Floating, isMinimized = false, WindowPosition.PlatformDefault, DpSize(1280.dp, 720.dp))
    var isWindowMaximized by remember {mutableStateOf(false)}
    var isDarkTheme = remember {mutableStateOf(true)}
    val themeColors = Colors().get(isDarkTheme)
    val (darkGray, middleGray, lightGray) = themeColors

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var isExpandedMenuItem by remember {mutableStateOf("")}
    val drawerWidth = if (isExpandedMenuItem != "") 0.8f else 0.2f

    var apiKey by remember {mutableStateOf("")}

    Window({exitApplication()}, windowState, visible = true, "LinkedIn Parser", undecorated = true) {
        ModalDrawer(
            drawerContent = {
                DrawerMenuContent(applicationScope, themeColors, isDarkTheme, drawerWidth, isExpandedMenuItem, apiKey, {apiKey = it}) {
                    isExpandedMenuItem =
                        when (isExpandedMenuItem) {
                            "Général" -> if (it == "Général") {""} else it.toString()
                            "Customisation" -> if (it == "Customisation") {""} else it.toString()
                            "Aide" -> if (it == "Aide") {""} else it.toString()
                            "Contact" -> if (it == "Contact") {""} else it.toString()
                            else -> it.toString()
                        }
                }
            },
            modifier = Modifier.fillMaxHeight(),
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerShape = RoundedCornerShape(0.dp, 25.dp, 25.dp, 0.dp),
            drawerElevation = 5.dp,
            drawerBackgroundColor = darkGray
        ) {
            Column(Modifier.fillMaxSize().background(darkGray)) {
                // Barre de titre
                WindowDraggableArea(Modifier.fillMaxWidth().height(45.dp).background(darkGray)) {
                    Row(Modifier.fillMaxSize().padding(15.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        AppTitleBar(themeColors, applicationScope, drawerState, windowState, isWindowMaximized,
                            onMinimize = {windowState.isMinimized = true},
                            onMaximizeOrRestore = {
                                windowState.placement = if (isWindowMaximized) WindowPlacement.Maximized else WindowPlacement.Floating
                                isWindowMaximized = !isWindowMaximized
                            },
                            onExit = {exitProcess(0)}
                        )
                    }
                }
                // Contenu principal
                Box(Modifier.fillMaxSize()) {
                    App(applicationScope, themeColors, apiKey)
                }
            }
        }
    }
}

@Composable
fun AppTitleBar(themeColors: List<Color>, applicationScope: CoroutineScope, drawerState: DrawerState, windowState: WindowState, isMaximized: Boolean, onMinimize: () -> Unit, onMaximizeOrRestore: () -> Unit, onExit: () -> Unit) {
    val (darkGray, middleGray, lightGray) = themeColors
    // Titre
    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        // Icone de menu
        IconButton({applicationScope.launch {if (drawerState.isOpen) drawerState.close() else drawerState.open()}}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Menu, "Menu", tint = lightGray)}
        // Spacer
        Spacer(Modifier.width(15.dp))
        // Texte
        Text("LinkedIn Parser", Modifier.height(15.dp), color = lightGray)
    }
    // Boutons
    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        // Minimiser
        IconButton({onMinimize()}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.KeyboardArrowDown, "Minimiser", tint = lightGray)}
        // Spacer
        Spacer(Modifier.width(15.dp))
        // Maximiser / Restaurer
        IconButton({onMaximizeOrRestore()}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Window, "Maximiser / Restaurer", tint = lightGray)}
        // Spacer
        Spacer(Modifier.width(15.dp))
        // Quitter
        IconButton({onExit()}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Close, "Quitter", tint = lightGray)}
    }
}