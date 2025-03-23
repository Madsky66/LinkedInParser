import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File

fun main() = application {
    val serverProcess = startPythonServer()
    Window(
        onCloseRequest = {
            serverProcess?.destroy()
            exitApplication()
        },
        title = "LinkedIn Parser"
    ) {
        App()
    }
}

fun startPythonServer(): Process? {
    return try {
        println("🔗 Démarrage du serveur WebSocket...")

        // Utiliser un chemin relatif plus robuste
        val serverPath = when {
            System.getProperty("os.name").lowercase().contains("windows") ->
                "src/main/resources/extra/server.exe"
            else ->
                "src/main/resources/extra/server"
        }

        val processBuilder = ProcessBuilder(serverPath)
        processBuilder.redirectErrorStream(true)

        // Ajouter la redirection de la sortie pour le debugging
        val logFile = File("server_log.txt")
        processBuilder.redirectOutput(logFile)

        val process = processBuilder.start()

        // Attendre que le serveur soit prêt
        Thread.sleep(2000)

        println("✅ Serveur Python démarré avec succès.")
        process
    }
    catch (e: Exception) {
        println("❌ Erreur lors du démarrage du serveur Python : ${e.message}")
        e.printStackTrace()
        null
    }
}