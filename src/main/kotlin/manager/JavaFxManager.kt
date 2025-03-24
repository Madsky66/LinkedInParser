package manager

import javafx.application.Platform

object JavaFxManager {
    private var initialized = false

    fun initialize() {
        if (!initialized) {
            try {
                // Vérifier si JavaFX est déjà initialisé
                try {
                    Platform.startup {}
                    initialized = true
                    println("✅ JavaFX initialisé avec succès")
                } catch (e: Exception) {
                    if (e.message?.contains("Toolkit already initialized") == true) {
                        // JavaFX est déjà initialisé, on considère que c'est OK
                        initialized = true
                        println("ℹ️ JavaFX était déjà initialisé")
                    } else {
                        throw e
                    }
                }
            } catch (e: Exception) {
                println("❌ Erreur d'initialisation de JavaFX: ${e.message}")
            }
        }
    }

    fun shutdown() {
        if (initialized) {
            try {
                Platform.exit()
                initialized = false
            } catch (e: Exception) {
                println("❌ Erreur lors de l'arrêt de JavaFX: ${e.message}")
            }
        }
    }

    fun isInitialized(): Boolean {
        return initialized
    }
}