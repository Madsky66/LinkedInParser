import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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

private var serverProcess: Process? = null

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

fun startPythonServer(): Process? {
    return try {
        // Vérification et création des dossiers nécessaires
        val extraDir = File("src/main/resources/extra")
        val chromeDir = File(extraDir, "chrome")
        if (!chromeDir.exists()) {throw Exception("Le dossier Chrome portable n'existe pas: ${chromeDir.absolutePath}")}
        val serverPath =
            if (System.getProperty("os.name").lowercase().contains("windows")) {"src/main/resources/extra/server.exe"} else {"src/main/resources/extra/server"}
        val serverFile = File(serverPath)
        if (!serverFile.exists()) {throw Exception("Le fichier serveur n'existe pas: $serverPath")}
        if (!serverFile.canExecute() && !serverFile.setExecutable(true)) {throw Exception("Impossible de rendre le serveur exécutable")}

        // Configuration de l'environnement pour le serveur Python
        val processBuilder = ProcessBuilder(serverPath)
        processBuilder.environment()["CHROME_PATH"] = chromeDir.absolutePath
        processBuilder.redirectErrorStream(true)
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)

        processBuilder.start().also {serverProcess = it}
    }
    catch (e: Exception) {
        println("❌ Erreur lors du démarrage du serveur: ${e.message}")
        null
    }
}

fun stopPythonServer(process: Process?) {
    try {
        process?.let {
            println("🛑 Arrêt du serveur Python...")
            it.destroy()
            if (!it.waitFor(5, TimeUnit.SECONDS)) {
                println("⚠️ Le serveur ne s'est pas arrêté, arrêt forcé...")
                it.destroyForcibly()
            }
        }
    }
    catch (e: Exception) {println("❌ Erreur lors de l'arrêt du serveur: ${e.message}")}
}