package manager

import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object ServerManager {
    private val logger = LoggerFactory.getLogger("ServerManager")
    var serverProcess: Process? = null
    var serverPid: Long? = null

    fun startServer(): Process? {
        return try {
            cleanupExistingServer()
            val extraDir = File("src/main/resources/extra")
            val chromeDir = File(extraDir, "chrome")
            if (!chromeDir.exists()) {throw IllegalStateException("Le dossier Chrome portable n'existe pas: ${chromeDir.absolutePath}")}
            val serverPath =
                if (System.getProperty("os.name").lowercase().contains("windows")) {"src/main/resources/extra/server.exe"}
                else {"src/main/resources/extra/server"}
            val serverFile = File(serverPath)
            if (!serverFile.exists()) {throw IllegalStateException("Le fichier serveur n'existe pas: $serverPath")}
            if (!serverFile.canExecute() && !serverFile.setExecutable(true)) {throw SecurityException("Impossible de rendre le serveur exécutable")}
            val processBuilder = ProcessBuilder(serverPath).apply {
                environment()["CHROME_PATH"] = chromeDir.absolutePath
                redirectErrorStream(true)
                redirectOutput(ProcessBuilder.Redirect.INHERIT)
            }
            val process = processBuilder.start()
            serverPid = process.pid()
            logger.info("✅ Serveur Python démarré avec PID: $serverPid")
            serverProcess = process
            process
        }
        catch (e: Exception) {
            logger.error("❌ Erreur lors du démarrage du serveur: ${e.message}", e)
            null
        }
    }

    fun cleanupExistingServer() {
        try {
            if (System.getProperty("os.name").lowercase().contains("windows")) {
                val process = Runtime.getRuntime().exec("taskkill /F /IM server.exe")
                process.waitFor(10, TimeUnit.SECONDS)
                if (process.exitValue() == 0) {logger.info("✅ Processus server.exe existant nettoyé avec succès")}
                else {logger.warn("⚠️ Aucun processus server.exe à nettoyer ou erreur lors de la suppression")}
            }
            else {Runtime.getRuntime().exec("pkill -f server")}
        }
        catch (e: IOException) {logger.warn("⚠️ Problème lors de la suppression du processus existant", e)}
        catch (e: InterruptedException) {
            logger.warn("⚠️ Processus interrompu pendant le nettoyage", e)
            Thread.currentThread().interrupt()
        }
    }

    fun stopServer() {
        try {
            serverProcess?.let {process ->
                logger.info("🛑 Arrêt du serveur Python...")
                if (System.getProperty("os.name").lowercase().contains("windows")) {
                    Runtime.getRuntime().exec("taskkill /F /PID $serverPid")
                    Runtime.getRuntime().exec("taskkill /F /IM server.exe")
                }
                else {Runtime.getRuntime().exec("pkill -f server")}
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                    logger.warn("⚠️ Le serveur Python n'a pas répondu à temps, arrêt forcé")
                }
                else {logger.info("✅ Serveur Python arrêté avec succès")}
            }
        }
        catch (e: Exception) {logger.error("❌ Erreur lors de l'arrêt du serveur: ${e.message}", e)}
    }
}