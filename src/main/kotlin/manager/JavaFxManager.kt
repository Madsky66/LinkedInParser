package manager

import javafx.application.Platform
import org.slf4j.LoggerFactory

object JavaFxManager {
    private var initialized = false
    private val logger = LoggerFactory.getLogger(JavaFxManager::class.java)

    fun initialize() {
        if (!initialized) {
            try {
                try {
                    Platform.startup {}
                    initialized = true
                    logger.info("JavaFX initialisé avec succès")
                }
                catch (e: Exception) {
                    if (e.message?.contains("Toolkit already initialized") == true) {
                        initialized = true
                        logger.info("JavaFX était déjà initialisé")
                    }
                    else {
                        logger.error("Erreur lors de l'initialisation de JavaFX", e)
                        throw e
                    }
                }
            }
            catch (e: Exception) {logger.error("Erreur d'initialisation de JavaFX: ${e.message}")}
        }
    }

    fun shutdown() {
        if (initialized) {
            try {
                Platform.exit()
                initialized = false
                logger.info("JavaFX arrêté avec succès")
            }
            catch (e: Exception) {logger.error("Erreur lors de l'arrêt de JavaFX: ${e.message}")}
        }
    }

    fun isInitialized(): Boolean {return initialized}
}