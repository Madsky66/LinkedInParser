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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Window
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import config.GlobalConfig
import config.GlobalInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ui.composable.App
import ui.composable.DrawerMenuContent
import kotlin.system.exitProcess

fun main() = application {
    val applicationScope: CoroutineScope = rememberCoroutineScope()
    val gC = GlobalInstance.config

    Window({exitApplication()}, gC.windowState.value, visible = true, "LinkedIn Parser", undecorated = true) {
        ModalDrawer(
            drawerContent = {
                DrawerMenuContent(applicationScope, gC) {
                    gC.isExpandedMenuItem.value =
                        when (gC.isExpandedMenuItem.value) {
                            "Général" -> if (it == "Général") {""} else it.toString()
                            "Customisation" -> if (it == "Customisation") {""} else it.toString()
                            "Aide" -> if (it == "Aide") {""} else it.toString()
                            "Contact" -> if (it == "Contact") {""} else it.toString()
                            else -> it.toString()
                        }
                }
            },
            modifier = Modifier.fillMaxHeight(),
            gesturesEnabled = true,
            drawerShape = RoundedCornerShape(0.dp, 25.dp, 25.dp, 0.dp),
            drawerElevation = 5.dp,
            drawerBackgroundColor = gC.darkGray.value
        ) {
            Column(Modifier.fillMaxSize().background(gC.darkGray.value)) {
                // Barre de titre
                WindowDraggableArea(Modifier.fillMaxWidth().height(50.dp).background(gC.darkGray.value)) {
                    Row(Modifier.fillMaxSize().padding(15.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        AppTitleBar(applicationScope, gC,
                            onMinimize = {gC.windowState.value.isMinimized = true},
                            onMaximizeOrRestore = {
                                gC.windowState.value.placement = if (gC.isWindowMaximized.value) WindowPlacement.Maximized else WindowPlacement.Floating
                                gC.isWindowMaximized.value = !gC.isWindowMaximized.value
                            },
                            onExit = {exitProcess(0)}
                        )
                    }
                }
                // Contenu principal
                Box(Modifier.fillMaxSize()) {
                    App(applicationScope, gC)
                }
            }
        }
    }
}

@Composable
fun AppTitleBar(applicationScope: CoroutineScope, gC: GlobalConfig, onMinimize: () -> Unit, onMaximizeOrRestore: () -> Unit, onExit: () -> Unit) {
    // Titre
    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        // Icone de menu
        IconButton({applicationScope.launch {if (gC.drawerState.value.isOpen) gC.drawerState.value.close() else gC.drawerState.value.open()}}, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Menu, "Menu", tint = gC.lightGray.value)}
        // Spacer
        Spacer(Modifier.width(15.dp))
        // Texte
        Text("LinkedIn Parser", fontSize = 15.sp, color = gC.lightGray.value)
    }
    // Boutons
    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        // Minimiser
        IconButton({onMinimize()}, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.KeyboardArrowDown, "Minimiser", tint = gC.lightGray.value)}
        // Spacer
        Spacer(Modifier.width(15.dp))
        // Maximiser / Restaurer
        IconButton({onMaximizeOrRestore()}, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Window, "Maximiser / Restaurer", tint = gC.lightGray.value)}
        // Spacer
        Spacer(Modifier.width(15.dp))
        // Quitter
        IconButton({onExit()}, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Close, "Quitter", tint = gC.lightGray.value)}
    }
}