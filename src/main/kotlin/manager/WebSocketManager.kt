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
            if (instance == null) {
                val port = getWebSocketPort()
                val uri = URI("ws://127.0.0.1:$port")
                instance = WebSocketManager(uri, onResult)
                instance?.connectBlocking(5, TimeUnit.SECONDS)
            }
        }

        fun sendProfileRequest(url: String) {
            instance?.let {
                if (it.isOpen) {
                    val request = Json.encodeToString(ProspectData(linkedinURL = url, status = "request"))
                    it.send(request)
                }
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