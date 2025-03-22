import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

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
        val processBuilder = ProcessBuilder("src/main/resources/extra/server.exe")
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
        println("✅ Serveur Python démarré avec succès.")
        process
    }
    catch (e: Exception) {
        println("❌ Erreur lors du démarrage du serveur Python : ${e.message}")
        null
    }
}