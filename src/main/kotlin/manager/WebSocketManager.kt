package manager

import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.java_websocket.client.WebSocketClient
import kotlin.concurrent.thread

class WebSocketManager(uri: URI) : WebSocketClient(uri) {
    var result: String = "En attente..."

    override fun onOpen(handshakedata: ServerHandshake?) {
        println("🔗 Connecté au serveur WebSocket")
    }

    override fun onMessage(message: String?) {
        message?.let {
            println("📥 Message reçu de Python : $it")
            val data = Json.decodeFromString<ProspectData>(it)
            result = "✅ Nom : ${data.name}, Email : ${data.email}"
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("❌ WebSocket fermé : $reason")
    }

    override fun onError(ex: Exception?) {
        println("⚠ Erreur WebSocket : ${ex?.message}")
    }
}

fun sendToPythonOverWebSocket(prospect: ProspectData, onResult: (String) -> Unit) {
    val webSocket = WebSocketManager(URI("ws://localhost:8765"))
    webSocket.connectBlocking()

    val jsonData = Json.encodeToString(prospect)
    webSocket.send(jsonData)

    println("📤 Données envoyées : $jsonData")

    thread {
        while (!webSocket.isClosed) {Thread.sleep(500)}
        onResult(webSocket.result)
    }
}