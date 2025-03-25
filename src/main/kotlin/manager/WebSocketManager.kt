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
import java.io.IOException
import java.net.ConnectException

class WebSocketManager(uri: URI, private val onResult: (String) -> Unit, private val scope: CoroutineScope) :
    WebSocketClient(uri) {
    companion object {
        private var instance: WebSocketManager? = null
        private val logger = LoggerFactory.getLogger(WebSocketManager::class.java)
        private const val DEFAULT_PORT = 9000
        private const val MAX_RETRIES = 5

        fun getWebSocketPort(): Int {
            val portFile = File("websocket_port.txt")
            return if (portFile.exists()) {
                try {portFile.readText().trim().toInt()}
                catch (e: NumberFormatException) {
                    logger.warn("Erreur de format lors de la lecture du port : ${e.message}, using default port $DEFAULT_PORT")
                    DEFAULT_PORT
                }
                catch (e: IOException) {
                    logger.warn("Erreur lors de la lecture du port : ${e.message}, using default port $DEFAULT_PORT")
                    DEFAULT_PORT
                }
            }
            else {DEFAULT_PORT}
        }

        @Synchronized
        fun initialize(onResult: (String) -> Unit, scope: CoroutineScope) {
            if (instance != null) {closeWebSocket()}
            val port = getWebSocketPort()
            val uri = URI("ws://127.0.0.1:$port")
            instance = WebSocketManager(uri, onResult, scope)
            for (attempt in 1..MAX_RETRIES) {
                try {
                    val connected = instance?.connectBlocking(2, TimeUnit.SECONDS) == true
                    if (connected) {
                        logger.info("Connecté au serveur WebSocket sur le port $port")
                        return
                    }
                    else {logger.warn("Tentative de connexion échouée (attempt $attempt)")}
                }
                catch (e: ConnectException) {logger.warn("Erreur de connexion (attempt $attempt): ${e.message}")}
                catch (e: Exception) {logger.error("Erreur lors de la connexion (attempt $attempt): ${e.message}", e)}
                Thread.sleep(1000)
            }
            logger.error("Échec de connexion au WebSocket après $MAX_RETRIES tentatives")
            instance = null
            throw IllegalStateException("Impossible de se connecter au serveur WebSocket")
        }

        private fun closeWebSocket() {
            try {instance?.close()}
            catch (e: Exception) {logger.error("Erreur lors de la fermeture du WebSocket: ${e.message}", e)}
            finally {instance = null}
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
                        logger.error("Erreur lors de l'envoi de la requête: ${e.message}, tentative de reconnexion...")
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
    override fun onError(ex: Exception?) {logger.error("Erreur WebSocket : ${ex?.message}", ex)}
    override fun onClose(code: Int, reason: String?, remote: Boolean) {logger.info("WebSocket fermé : $reason")}
}