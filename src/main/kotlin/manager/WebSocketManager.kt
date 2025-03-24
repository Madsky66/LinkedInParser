package manager

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import data.ProspectData
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit

class WebSocketManager(uri: URI, private val onResult: (String) -> Unit) : WebSocketClient(uri) {
    companion object {
        private var instance: WebSocketManager? = null

        fun getWebSocketPort(): Int {
            val portFile = File("websocket_port.txt")
            return if (portFile.exists()) {
                try {portFile.readText().trim().toInt()}
                catch (e: Exception) {
                    println("⚠️ Erreur lors de la lecture du port : ${e.message}")
                    9000
                }
            }
            else {9000}
        }

        fun initialize(onResult: (String) -> Unit) {
            try {
                instance?.close()
                instance = null
                val port = getWebSocketPort()
                val uri = URI("ws://127.0.0.1:$port")
                instance = WebSocketManager(uri, onResult)
                var attempts = 0
                while (attempts < 5) {
                    try {
                        val connected = instance?.connectBlocking(2, TimeUnit.SECONDS)
                        if (connected == true) {
                            println("✅ Connecté au serveur WebSocket sur le port $port")
                            return
                        }
                    }
                    catch (e: Exception) {println("⚠️ Tentative de connexion échouée: ${e.message}")}
                    attempts++
                    Thread.sleep(1000)
                }
                println("❌ Échec de connexion au WebSocket après 5 tentatives")
                instance = null
                throw Exception("Impossible de se connecter au serveur WebSocket")
            }
            catch (e: Exception) {
                println("❌ Erreur lors de l'initialisation du WebSocket: ${e.message}")
                instance = null
                throw e
            }
        }

        fun sendProfileRequest(url: String) {
            instance?.let {
                if (it.isOpen) {
                    try {
                        val request = Json.encodeToString(ProspectData(linkedinURL = url, status = "request"))
                        it.send(request)
                        println("📤 Requête envoyée pour l'URL: $url")
                    }
                    catch (e: Exception) {
                        println("❌ Erreur lors de l'envoi de la requête: ${e.message}")
                        initialize(it.onResult)
                    }
                }
                else {
                    println("⚠️ WebSocket non connecté, tentative de reconnexion...")
                    initialize(it.onResult)
                }
            } ?: run {
                println("⚠️ WebSocketManager non initialisé, tentative d'initialisation...")
                initialize {result -> println("📥 Résultat reçu: $result")}
            }
        }
    }

    override fun onOpen(handshakedata: ServerHandshake?) {println("✅ Connecté au serveur WebSocket")}
    override fun onMessage(message: String?) {
        message?.let {
            println("📥 Message reçu : $it")
            onResult(it)
        }
    }
    override fun onError(ex: Exception?) {println("❌ Erreur WebSocket : ${ex?.message}")}
    override fun onClose(code: Int, reason: String?, remote: Boolean) {println("🔌 WebSocket fermé : $reason")}
}