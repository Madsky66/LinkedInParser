import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import ui.composable.App

private val logger = LoggerFactory.getLogger("Main")

fun main() = application {
    System.setProperty("networkaddress.cache.ttl", "60")
    val windowState = rememberWindowState(size = DpSize(1280.dp, 720.dp))
    val appJob = SupervisorJob()
    val applicationScope = CoroutineScope(Dispatchers.Default + appJob + CoroutineExceptionHandler {_, e ->
        logger.error("❌ Erreur générale de l'application : ${e.message}", e)
        exitProcess(1)
    })
    Thread.setDefaultUncaughtExceptionHandler {_, e ->
        logger.error("❌ Exception non gérée : ${e.message}", e)
        exitProcess(1)
    }

    Window(
        onCloseRequest = {
            applicationScope.launch {
                appJob.cancel()
                exitApplication()
            }
        },
        title = "LinkedIn Parser",
        state = windowState,
        onPreviewKeyEvent = {false}
    ) {
        App(windowState, applicationScope)
    }
}