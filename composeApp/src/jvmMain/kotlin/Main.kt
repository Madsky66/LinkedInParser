import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import config.GlobalInstance.config as gC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ui.composable.App
import ui.composable.AppTitleBar
import ui.composable.drawer.DrawerMenuContent
import kotlin.system.exitProcess

fun onToggleDrawer(applicationScope: CoroutineScope, drawerState: BottomDrawerState) {
    applicationScope.launch {
        if (drawerState.isClosed) {drawerState.expand()}
        else {drawerState.close()}
    }
}
fun onMinimizeWindow(windowState: WindowState) {windowState.isMinimized = true}
fun onToggleMaximizeOrRestore(windowState: WindowState) {
    if (windowState.placement == WindowPlacement.Maximized) {windowState.placement = WindowPlacement.Floating}
    else {windowState.placement = WindowPlacement.Maximized}
}
fun onCloseApp() {exitProcess(0)}

fun main() = application {
    val applicationScope: CoroutineScope = rememberCoroutineScope()
    var windowState by remember {mutableStateOf(WindowState(WindowPlacement.Floating, false, WindowPosition.PlatformDefault, DpSize(1280.dp, 720.dp)))}
    val drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
    val darkGray = gC.darkGray.value

    Window({exitApplication()}, windowState, visible = true, "LinkedIn Parser", undecorated = true) {
        Column(Modifier.fillMaxSize().background(Color.Red)) {
            // Barre de titre
            WindowDraggableArea(Modifier.fillMaxWidth().height(50.dp).background(darkGray)) {
                Row(Modifier.fillMaxSize()) {
                    AppTitleBar(
                        gC = gC,
                        onToggleDrawer = {onToggleDrawer(applicationScope, drawerState)},
                        onMinimizeWindow = {onMinimizeWindow(windowState)},
                        onToggleMaximizeOrRestore = {onToggleMaximizeOrRestore(windowState)},
                        onCloseApp = {onCloseApp()}
                    )
                }
            }
            // Menu
            BottomDrawer(
                drawerContent = {DrawerMenuContent(applicationScope)},
                modifier = Modifier.fillMaxSize(),
                drawerState = drawerState,
                gesturesEnabled = true,
                drawerShape = RoundedCornerShape(0.dp)
            ) {
                Column(Modifier.fillMaxSize()) {App(applicationScope)}
            }
//            // Ancien menu
//            ModalDrawer({DrawerMenuContent(applicationScope)}, Modifier.background(Color.Green).fillMaxSize(0.99f)) {
//                Column(Modifier.fillMaxSize(0.99f)) {App(applicationScope)}
//            }
        }
    }
}