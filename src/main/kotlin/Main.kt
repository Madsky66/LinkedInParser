import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import ui.composable.App
import ui.theme.Colors

private val logger = LoggerFactory.getLogger("Main")

fun main() = application {
    System.setProperty("networkaddress.cache.ttl", "60")
    val windowState = rememberWindowState(size = DpSize(1280.dp, 720.dp))
    val appJob = SupervisorJob()
    val applicationScope = CoroutineScope(Dispatchers.Default + appJob + CoroutineExceptionHandler {_, e -> logger.error("❌ Erreur générale de l'application : ${e.message}", e); exitProcess(1)})

    Thread.setDefaultUncaughtExceptionHandler {_, e -> logger.error("❌ Exception non gérée : ${e.message}", e); exitProcess(1)}

    Window(
        onCloseRequest = {applicationScope.launch {appJob.cancel(); exitApplication()}},
        visible = true,
        state = windowState,
        title = "LinkedIn Parser",
        undecorated = true,
        onPreviewKeyEvent = {false}
    ) {
        Column(Modifier.fillMaxSize()) {
            WindowDraggableArea(Modifier.fillMaxWidth().height(45.dp).background(Colors().DARK_GRAY).align(Alignment.CenterHorizontally)) {
                Row(Modifier.fillMaxSize().padding(15.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("LinkedIn Parser", Modifier.height(15.dp), color = Color.LightGray)
                    Row {
                        IconButton(
                            onClick = {
                                //                        val current = AppManager.focusedWindow
                                //                        if (current != null) {current.window.setExtendedState(JFrame.ICONIFIED)}
                            },
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(100))
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "Réduire", tint = Color.LightGray)
                        }
                        Spacer(Modifier.width(15.dp))
                        IconButton(
                            onClick = {
                                //                        val current = AppManager.focusedWindow
                                //                        if (current != null) {
                                //                            if (current.window.extendedState == JFrame.MAXIMIZED_BOTH) {current.window.setExtendedState(JFrame.NORMAL)}
                                //                            else {current.window.setExtendedState(JFrame.MAXIMIZED_BOTH)}
                                //                        }
                            },
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(100))
                        ) {
                            Icon(
//                                val current = AppManager.focusedWindow
//                                if (current != null) {Icons.AutoMirrored.Filled.ArrowBack}
//                                else {Icons.AutoMirrored.Filled.ArrowForward},
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Maximiser / Minimiser",
                                tint = Color.LightGray
                            )
                        }
                        Spacer(Modifier.width(15.dp))
                        IconButton(
                            onClick = {
                                //                    AppManager.focusedWindow?.close()
                            },
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(100))
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Quitter", tint = Color.LightGray)
                        }
                    }
                }
            }
            App(windowState, applicationScope)
        }
    }
}