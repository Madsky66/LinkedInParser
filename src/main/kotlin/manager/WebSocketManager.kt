package manager

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import data.ProspectData
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class WebSocketManager(uri: URI, private val onResult: (String) -> Unit) : WebSocketClient(uri) {
    companion object {
        private var instance: WebSocketManager? = null
        private const val MAX_RETRY = 5
        private const val RETRY_DELAY = 1000L // 1 seconde

        fun getWebSocketPort(): Int {
            val portFile = File("websocket_port.txt")
            return if (portFile.exists()) {
                try {
                    portFile.readText().trim().toInt()
                } catch (e: Exception) {
                    println("⚠️ Erreur lors de la lecture du port : ${e.message}")
                    9000
                }
            } else {
                9000
            }
        }

        fun initialize(onResult: (String) -> Unit) {
            if (instance == null) {
                var retryCount = 0
                var connected = false

                while (!connected && retryCount < MAX_RETRY) {
                    try {
                        val port = getWebSocketPort()
                        val uri = URI("ws://127.0.0.1:$port")
                        instance = WebSocketManager(uri, onResult)

                        // Attendre que le serveur soit prêt
                        runBlocking { delay(500) }

                        connected = instance?.connectBlocking(3, TimeUnit.SECONDS) ?: false

                        if (connected) {
                            println("✅ Connecté au serveur WebSocket sur le port $port")
                        } else {
                            println("⚠️ Échec de connexion au WebSocket, tentative ${retryCount + 1}/$MAX_RETRY")
                            runBlocking { delay(RETRY_DELAY) }
                            retryCount++
                        }
                    } catch (e: Exception) {
                        println("❌ Erreur de connexion WebSocket: ${e.message}")
                        runBlocking { delay(RETRY_DELAY) }
                        retryCount++
                    }
                }

                if (!connected) {
                    println("❌ Impossible de se connecter au serveur WebSocket après $MAX_RETRY tentatives")
                }
            }
        }

        fun sendProfileRequest(url: String) {
            instance?.let {
                if (it.isOpen) {
                    val request = Json.encodeToString(ProspectData(linkedinURL = url, status = "request"))
                    it.send(request)
                    println("📤 Requête envoyée pour l'URL: $url")
                } else {
                    println("⚠️ WebSocket non connecté, impossible d'envoyer la requête")
                    // Tentative de reconnexion
                    it.reconnect()
                }
            } ?: println("❌ WebSocketManager non initialisé")
        }
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        println("✅ Connecté au serveur WebSocket")
    }

    override fun onMessage(message: String?) {
        message?.let {
            println("📥 Message reçu : $it")
            onResult(it)
        }
    }

    override fun onError(ex: Exception?) {
        println("❌ Erreur WebSocket : ${ex?.message}")
        ex?.printStackTrace()
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("🔌 WebSocket fermé : $reason (code: $code, distant: $remote)")
    }
}