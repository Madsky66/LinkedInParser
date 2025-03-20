package manager

import kotlinx.serialization.json.Json
import java.net.URI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import kotlinx.serialization.encodeToString

class WebSocketManager(uri: URI) : WebSocketClient(uri) {
    var result = "En attente..."

    override fun onOpen(handshakedata: ServerHandshake?) {
        println("🔗 Connecté au serveur WebSocket")
    }

    override fun onMessage(message: String?) {
        message?.let {
            println("📥 Message reçu de Python : $it")
            val data = Json.decodeFromString<ProspectData>(it)
            result = "✅ Nom : ${data.name}, Email : ${data.email}"
            println(result)
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("❌ WebSocket fermé : $reason")
    }

    override fun onError(ex: Exception?) {
        println("⚠ Erreur WebSocket : ${ex?.message}")
    }
}

fun sendToPythonOverWebSocket(prospect: ProspectData) {
    val webSocket = WebSocketManager(URI("ws://localhost:8765"))
    webSocket.connectBlocking()

    val jsonData = Json.encodeToString(prospect)
    webSocket.send(jsonData)
}