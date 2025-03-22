package manager

import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.java_websocket.client.WebSocketClient
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import data.ProspectData

class WebSocketManager(uri: URI, private val onResult: (String) -> Unit) : WebSocketClient(uri) {
    private val latch = CountDownLatch(1)
    var result: String = "En attente..."

    override fun onOpen(handshakedata: ServerHandshake?) {println("🔗 Connecté au serveur WebSocket")}

    override fun onMessage(message: String?) {
        message?.let {
            println("📥 Message reçu de Python : $it")
            try {
                val data = Json.decodeFromString<ProspectData>(it)
                result = "✅ Nom : ${data.name}, Email : ${data.email}"
            }
            catch (e: Exception) {
                result = "❌ Erreur de traitement du message."
                println("❌ Erreur de parsing : ${e.message}")
            }
            latch.countDown()
        } ?: run {
            result = "❌ Message vide reçu."
            latch.countDown()
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("❌ WebSocket fermé : $reason")
        latch.countDown()
    }

    override fun onError(ex: Exception?) {
        ex?.let {
            println("⚠ Erreur WebSocket : ${it.message}")
            result = "⚠ Erreur de connexion au serveur WebSocket."
            latch.countDown()
        }
    }

    fun waitForResult(): String {
        latch.await()
        return result
    }
}

fun sendToPythonOverWebSocket(prospect: ProspectData, onResult: (String) -> Unit) {
    try {
        val webSocket = WebSocketManager(URI("ws://localhost:8765"), onResult)
        println("🔗 Connexion au serveur WebSocket...")

        webSocket.connectBlocking()
        println("✅ Connexion WebSocket établie avec succès.")

        if (webSocket.isOpen) {
            val jsonData = Json.encodeToString(prospect)
            println("📤 Données envoyées : $jsonData")
            webSocket.send(jsonData)

            thread {
                val result = webSocket.waitForResult()
                println("📥 Résultat reçu : $result")
                onResult(result)
                println("🔗 Déconnexion du serveur WebSocket...")
                webSocket.close()
            }
        }
        else {
            println("❌ Connexion WebSocket impossible")
            onResult("⚠ Impossible de se connecter au serveur WebSocket.")
        }
    }
    catch (e: Exception) {
        println("❌ Erreur WebSocket : ${e.message}")
        onResult("⚠ Erreur de connexion au serveur WebSocket.")
    }
}
