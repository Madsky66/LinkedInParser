import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import config.GlobalInstance.config as gC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ui.composable.App
import ui.composable.drawer.DrawerMenuContent
import kotlin.system.exitProcess

fun main() = application {
    val applicationScope: CoroutineScope = rememberCoroutineScope()

    Window({exitApplication()}, gC.windowState.value, visible = true, "LinkedIn Parser", undecorated = true) {
        ModalDrawer(
            drawerContent = {DrawerMenuContent(applicationScope)},
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
                        AppTitleBar(applicationScope)
                    }
                }
                // Contenu principal
                Box(Modifier.fillMaxSize()) {
                    App(applicationScope)
                }
            }
        }
    }
}

@Composable
fun AppTitleBar(applicationScope: CoroutineScope) {
    val lightGray = gC.lightGray.value
    var isMinimized by gC.windowState.value::isMinimized

    // Titre
    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        // Icone de menu
        IconButton({applicationScope.launch {if (gC.drawerState.value.isOpen) gC.drawerState.value.close() else gC.drawerState.value.open()}}, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Menu, "Menu", tint = lightGray)}
        // Spacer
        Spacer(Modifier.width(15.dp))
        // Texte
        Text("LinkedIn Parser", fontSize = 15.sp, color = lightGray)
    }
    // Boutons
    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        // Minimiser
        IconButton({isMinimized = true}, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.KeyboardArrowDown, "Minimiser", tint = lightGray)}
        // Spacer
        Spacer(Modifier.width(15.dp))
        // Maximiser / Restaurer
        IconButton({gC.windowState.value.placement = if (isMinimized) WindowPlacement.Maximized else WindowPlacement.Floating; gC.isWindowMaximized.value = !gC.isWindowMaximized.value}, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Window, "Maximiser / Restaurer", tint = lightGray)}
        // Spacer
        Spacer(Modifier.width(15.dp))
        // Quitter
        IconButton({exitProcess(0)}, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Close, "Quitter", tint = lightGray)}
    }
}