import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpSize
import ui.composable.App
import ui.theme.Colors
import kotlin.system.exitProcess

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(1280.dp, 720.dp))

    Window(
        onCloseRequest = {exitApplication()},
        visible = true,
        state = windowState,
        title = "LinkedIn Parser",
        undecorated = true,
    ) {
        var isMaximized by remember {mutableStateOf(false)}
        Column(Modifier.fillMaxSize()) {
            WindowDraggableArea(Modifier.fillMaxWidth().height(45.dp).background(Colors().DARK_GRAY).align(Alignment.CenterHorizontally)) {
                Row(Modifier.fillMaxSize().padding(15.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("LinkedIn Parser", Modifier.height(15.dp), color = Color.LightGray)
                    Row {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(100))
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "Minimiser", tint = Color.LightGray)
                        }

                        Spacer(Modifier.width(15.dp))

                        IconButton(
                            onClick = {
                                isMaximized = !isMaximized
                                if (isMaximized) {windowState.size = DpSize(1920.dp, 1080.dp)}
                                else {windowState.size = DpSize(1280.dp, 720.dp)}
                            },
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(100))
                        ) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Maximiser / Restaurer", tint = Color.LightGray)
                        }

                        Spacer(Modifier.width(15.dp))

                        IconButton(
                            onClick = {exitProcess(0)},
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(100))
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Quitter", tint = Color.LightGray)
                        }
                    }
                }
            }
            Box(Modifier.fillMaxSize()) {
                App()
            }
        }
    }
}