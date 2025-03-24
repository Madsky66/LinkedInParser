import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.composable.App
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

fun main() = application {
    val serverProcess = startPythonServer()

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        println("❌ Exception non gérée : ${e.message}")
        stopPythonServer(serverProcess)
        cleanupResources()
        exitProcess(1)
    }

    Window(
        onCloseRequest = {
            // Arrêt propre des processus
            stopPythonServer(serverProcess)
            cleanupResources()
            exitApplication()
        },
        title = "LinkedIn Parser"
    ) {
        App()
    }
}

private fun cleanupResources() {
    try {
        // Nettoyage des fichiers temporaires de Chrome si nécessaire
        val tempDir = Paths.get("src/main/resources/extra/chrome/temp")
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.delete(it) }
        }
    } catch (e: Exception) {
        println("⚠️ Erreur lors du nettoyage des ressources: ${e.message}")
    }
}

private var serverPid: Long? = null

fun startPythonServer(): Process? {
    return try {
        // Vérification des processus existants et nettoyage si nécessaire
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
            Runtime.getRuntime().exec("taskkill /F /IM server.exe")
            Thread.sleep(1000)
        }
        catch (e: Exception) {println("⚠️ Pas de processus server.exe existant à nettoyer")}
    }
}

fun stopPythonServer(process: Process?) {
    try {
        // Arrêt du processus principal
        process?.let {
            println("🛑 Arrêt du serveur Python...")
            if (System.getProperty("os.name").lowercase().contains("windows")) {
                Runtime.getRuntime().exec("taskkill /F /PID $serverPid")
                Runtime.getRuntime().exec("taskkill /F /IM server.exe")
            }
            else {it.destroy()}
            if (!it.waitFor(5, TimeUnit.SECONDS)) {it.destroyForcibly()}
        }
    }
    catch (e: Exception) {println("❌ Erreur lors de l'arrêt du serveur: ${e.message}")}
}