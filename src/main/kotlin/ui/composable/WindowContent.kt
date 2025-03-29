package ui.composable

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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@Composable
fun WindowContent(windowState: WindowState, applicationScope: CoroutineScope, drawerState: DrawerState, themeColors: List<Color>, apiKey: String?) {
    val (darkGray, middleGray, lightGray) = themeColors
    var isMaximized by remember {mutableStateOf(false)}

    Window(onCloseRequest = {/*exitApplication()*/}, state = windowState, visible = true, title = "LinkedIn Parser", undecorated = true) {

        Column(Modifier.fillMaxSize()) {
            // Barre de titre
            WindowDraggableArea(Modifier.fillMaxWidth().height(45.dp).background(darkGray)) {
                Row(Modifier.fillMaxSize().padding(15.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    // Titre
                    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Icone de menu
                        IconButton(onClick = {applicationScope.launch {if (drawerState.isOpen) drawerState.close() else drawerState.open()}}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Menu, "Menu", tint = lightGray)}
                        Spacer(Modifier.width(15.dp))
                        // Texte
                        Text("LinkedIn Parser", Modifier.height(15.dp), color = lightGray)
                    }
                    // Boutons
                    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Minimiser
                        IconButton(onClick = {windowState.isMinimized = true}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.KeyboardArrowDown, "Minimiser", tint = lightGray)}
                        Spacer(Modifier.width(15.dp))
                        // Maximiser / Restaurer
                        IconButton(onClick = {isMaximized = !isMaximized; windowState.placement = if (isMaximized) WindowPlacement.Maximized else WindowPlacement.Floating}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Window, "Maximiser / Restaurer", tint = lightGray)}
                        Spacer(Modifier.width(15.dp))
                        // Quitter
                        IconButton(onClick = {exitProcess(0)}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Close, "Quitter", tint = lightGray)}
                    }
                }
            }
            // Contenu principal
            Box(Modifier.fillMaxSize()) {
                App(applicationScope, themeColors, apiKey)
            }
        }
    }
}