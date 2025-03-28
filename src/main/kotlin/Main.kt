import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.rememberDrawerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ui.composable.App
import ui.theme.Colors
import kotlin.system.exitProcess

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(1280.dp, 720.dp))
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val applicationScope: CoroutineScope = rememberCoroutineScope()

    Window(onCloseRequest = {exitApplication()}, visible = true, state = windowState, title = "LinkedIn Parser", undecorated = true) {
        var isMaximized by remember {mutableStateOf(false)}
        Column(Modifier.fillMaxSize()) {
            // Barre de titre
            WindowDraggableArea(Modifier.fillMaxWidth().height(45.dp).background(Colors().DARK_GRAY)) {
                Row(Modifier.fillMaxSize().padding(15.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {

                    // Titre
                    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {

                        // Icone de menu
                        IconButton(
                            onClick = {applicationScope.launch {if (drawerState.isOpen) drawerState.close() else drawerState.open()}},
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(100))
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.LightGray)
                        }
                        Spacer(Modifier.width(15.dp))

                        // Texte
                        Text("LinkedIn Parser", Modifier.height(15.dp), color = Color.LightGray)
                    }

                    // Boutons
                    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {

                        // Minimiser
                        IconButton(
                            onClick = {windowState.isMinimized = true},
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(100))
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Minimiser", tint = Color.LightGray)
                        }
                        Spacer(Modifier.width(15.dp))

                        // Maximiser / Restaurer
                        IconButton(
                            onClick = {isMaximized = !isMaximized; windowState.placement = if (isMaximized) WindowPlacement.Maximized else WindowPlacement.Floating},
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(100))
                        ) {
                            Icon(Icons.Filled.Window, contentDescription = "Maximiser / Restaurer", tint = Color.LightGray)
                        }
                        Spacer(Modifier.width(15.dp))

                        // Quitter
                        IconButton(
                            onClick = {exitProcess(0)},
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(100))
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Quitter", tint = Color.LightGray)
                        }
                    }
                }
            }

            // Contenu principal
            Box(Modifier.fillMaxSize()) {
                App(applicationScope)
            }
        }
    }
}