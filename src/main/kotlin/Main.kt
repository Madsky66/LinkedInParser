import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import manager.JavaFxManager
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import ui.composable.App
import java.io.File
import java.io.IOException

private val logger = LoggerFactory.getLogger("Main")

fun launchChrome() {
    val chromePath = "C:\\Users\\Pierr\\Desktop\\FREELANCE\\Madsky\\Code\\IntelliJ IDEA\\Apps\\LinkedInParser\\resources\\extra\\chrome\\chrome.exe"
    val linkedInLoginUrl = "https://www.linkedin.com/login"

    val chromeFile = File(chromePath)
    if (!chromeFile.exists()) {
        println("Chrome executable not found at: $chromePath")
        return
    }

    try {
        val processBuilder = ProcessBuilder("\"$chromePath\"", linkedInLoginUrl)
        processBuilder.start()
    } catch (e: IOException) {
        println("Error launching Chrome: ${e.message}")
    }
}

fun main() = application {
    System.setProperty("networkaddress.cache.ttl", "60")
    JavaFxManager.initialize()

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