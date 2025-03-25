package manager

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import data.ProspectData
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

class WebSocketManager(uri: URI, private val onResult: (String) -> Unit, private val scope: CoroutineScope) : WebSocketClient(uri) {
    companion object {
        private var instance: WebSocketManager? = null
        private val logger = LoggerFactory.getLogger(WebSocketManager::class.java)

        fun getWebSocketPort(): Int {
            val portFile = File("websocket_port.txt")
            return if (portFile.exists()) {
                try {portFile.readText().trim().toInt()}
                catch (e: Exception) {
                    logger.warn("Erreur lors de la lecture du port : ${e.message}, using default port 9000")
                    9000
                }
            }
            else {9000}
        }

        fun initialize(onResult: (String) -> Unit, scope: CoroutineScope) {
            try {
                instance?.close()
                instance = null
                val port = getWebSocketPort()
                val uri = URI("ws://127.0.0.1:$port")
                instance = WebSocketManager(uri, onResult, scope)
                var attempts = 0
                while (attempts < 5) {
                    try {
                        val connected = instance?.connectBlocking(2, TimeUnit.SECONDS)
                        if (connected == true) {
                            logger.info("Connecté au serveur WebSocket sur le port $port")
                            return
                        }
                    }
                    catch (e: Exception) {logger.warn("Tentative de connexion échouée: ${e.message}")}
                    attempts++
                    Thread.sleep(1000)
                }
                logger.error("Échec de connexion au WebSocket après 5 tentatives")
                instance = null
                throw Exception("Impossible de se connecter au serveur WebSocket")
            }
            catch (e: Exception) {
                logger.error("Erreur lors de l'initialisation du WebSocket: ${e.message}")
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
                        logger.info("Requête envoyée pour l'URL: $url")
                    }
                    catch (e: Exception) {
                        logger.error("Erreur lors de l'envoi de la requête: ${e.message}")
                        initialize(it.onResult, it.scope)
                    }
                }
                else {
                    logger.warn("WebSocket non connecté, tentative de reconnexion...")
                    initialize(it.onResult, it.scope)
                }
            } ?: run {
                logger.warn("WebSocketManager non initialisé, tentative d'initialisation...")
                initialize({result -> logger.info("Résultat reçu: $result")}, MainScope())
            }
        }
    }

    override fun onOpen(handshakedata: ServerHandshake?) {logger.info("Connecté au serveur WebSocket")}
    override fun onMessage(message: String?) {
        message?.let {
            logger.info("Message reçu : $it")
            onResult(it)
        }
    }
    override fun onError(ex: Exception?) {logger.error("Erreur WebSocket : ${ex?.message}")}
    override fun onClose(code: Int, reason: String?, remote: Boolean) {logger.info("WebSocket fermé : $reason")}
}