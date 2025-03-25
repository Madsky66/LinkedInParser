import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.composable.App
import manager.JavaFxManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlinx.coroutines.*

fun main() = application {
    JavaFxManager.initialize()

    val windowState = rememberWindowState()
    var serverProcess: Process? = null
    val exceptionHandler = CoroutineExceptionHandler {_, e ->
        println("❌ Exception non gérée : ${e.message}")
        stopPythonServer(serverProcess)
        cleanupResources()
        JavaFxManager.shutdown()
        exitProcess(1)
    }
    val applicationScope = CoroutineScope(Dispatchers.Default + exceptionHandler)

    serverProcess = startPythonServer()

    Thread.setDefaultUncaughtExceptionHandler {_, e ->
        println("❌ Exception non gérée : ${e.message}")
        stopPythonServer(serverProcess)
        cleanupResources()
        JavaFxManager.shutdown()
        exitProcess(1)
    }

    Window(
        onCloseRequest = {
            applicationScope.launch {
                stopPythonServer(serverProcess)
                cleanupResources()
                JavaFxManager.shutdown()
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

private fun cleanupResources() {
    try {
        val tempDir = Paths.get("src/main/resources/extra/chrome/temp")
        if (Files.exists(tempDir)) {
            Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach {
                try {Files.delete(it)}
                catch (e: Exception) {println("⚠️ Erreur lors de la suppression du fichier ${it}: ${e.message}")}
            }
        }
    }
    catch (e: Exception) {println("⚠️ Erreur lors du nettoyage des ressources: ${e.message}")}
}

private var serverPid: Long? = null

fun startPythonServer(): Process? {
    return try {
        cleanupExistingServer()

        val extraDir = File("src/main/resources/extra")
        val chromeDir = File(extraDir, "chrome")
        if (!chromeDir.exists()) {throw Exception("Le dossier Chrome portable n'existe pas: ${chromeDir.absolutePath}")}

        val serverPath =
            if (System.getProperty("os.name").lowercase().contains("windows")) {"src/main/resources/extra/server.exe"}
            else {"src/main/resources/extra/server"}
        val serverFile = File(serverPath)
        if (!serverFile.exists()) {throw Exception("Le fichier serveur n'existe pas: $serverPath")}
        if (!serverFile.canExecute() && !serverFile.setExecutable(true)) {throw Exception("Impossible de rendre le serveur exécutable")}

        val processBuilder = ProcessBuilder(serverPath)
        processBuilder.environment()["CHROME_PATH"] = chromeDir.absolutePath
        processBuilder.redirectErrorStream(true)
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)

        val process = processBuilder.start()
        serverPid = process.pid()
        println("✅ Serveur Python démarré avec PID: ${serverPid}")
        process
    }
    catch (e: Exception) {
        println("❌ Erreur lors du démarrage du serveur: ${e.message}")
        null
    }
}

private fun cleanupExistingServer() {
    if (System.getProperty("os.name").lowercase().contains("windows")) {
        try {
            val process = Runtime.getRuntime().exec("taskkill /F /IM server.exe")
            process.waitFor(10, TimeUnit.SECONDS)
            if (process.exitValue() == 0) {println("✅ Processus server.exe existant nettoyé avec succès")}
            else {println("⚠️ Pas de processus server.exe existant à nettoyer ou erreur lors de la suppression")}
        }
        catch (e: Exception) {println("⚠️ Pas de processus server.exe existant à nettoyer")}
    }
}

fun stopPythonServer(process: Process?) {
    try {
        process?.let {
            println("🛑 Arrêt du serveur Python...")
            if (System.getProperty("os.name").lowercase().contains("windows")) {
                try {
                    Runtime.getRuntime().exec("taskkill /F /PID $serverPid")
                    Runtime.getRuntime().exec("taskkill /F /IM server.exe")
                }
                catch (e: Exception) {println("⚠️ Erreur lors de la tentative d'arrêt du processus server.exe: ${e.message}")}
            }
            else {it.destroy()}
            if (!it.waitFor(5, TimeUnit.SECONDS)) {
                it.destroyForcibly()
                println("⚠️ Le serveur Python n'a pas répondu à temps, forçant l'arrêt")
            }
            else {println("✅ Serveur Python arrêté avec succès")}
        }
    }
    catch (e: Exception) {println("❌ Erreur lors de l'arrêt du serveur: ${e.message}")}
}