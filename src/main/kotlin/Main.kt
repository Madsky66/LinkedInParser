import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.composable.App
import manager.ServerManager
import manager.JavaFxManager
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Main")

fun main() = application {
    System.setProperty("networkaddress.cache.ttl", "60")
    JavaFxManager.initialize()

    val windowState = rememberWindowState()
    val appJob = SupervisorJob()
    val applicationScope = CoroutineScope(Dispatchers.Default + appJob + CoroutineExceptionHandler {_, e ->
        logger.error("❌ Erreur globale dans l'application : ${e.message}", e)
        ServerManager.stopServer()
        JavaFxManager.shutdown()
        exitProcess(1)
    })

    ServerManager.startServer()

    Thread.setDefaultUncaughtExceptionHandler {_, e ->
        logger.error("❌ Exception non gérée : ${e.message}", e)
        ServerManager.stopServer()
        JavaFxManager.shutdown()
        exitProcess(1)
    }

    Window(
        onCloseRequest = {
            applicationScope.launch {
                ServerManager.stopServer()
                JavaFxManager.shutdown()
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