package manager

import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.client.WebSocketClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.net.URI
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import data.ProspectData

class WebSocketManager(uri: URI, private val onResult: (String) -> Unit) : WebSocketClient(uri) {
    private val latch = CountDownLatch(1)
    var result: String = "En attente..."

    override fun onOpen(handshakedata: ServerHandshake?) {println("✅ Connecté au serveur WebSocket")}

    override fun onMessage(message: String?) {
        message?.let {
            println("📥 Message reçu : $it")
            try {
                val data = Json.decodeFromString<ProspectData>(it)
                result = "✅ ${data.name} - ${data.email}"
            }
            catch (e: Exception) {
                result = "❌ Erreur de parsing JSON."
                println("❌ Erreur de parsing : ${e.message}")
            }
            onResult(result)
            latch.countDown()
        } ?: run {
            result = "❌ Message vide reçu."
            onResult(result)
            latch.countDown()
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("❌ WebSocket fermé : $reason")
        latch.countDown()
    }

    override fun onError(ex: Exception?) {
        ex?.let {
            println("[WebSocket] Message : \"${it.message}\" | \"${it.stackTrace}\" | \"${it.cause}\"")
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
        val webSocket = WebSocketManager(URI("ws://localhost:9000"), onResult)
        println("🔗 Connexion WebSocket...")
        webSocket.connectBlocking()
        if (webSocket.isOpen) {
            println("✅ Connexion établie avec au serveur WebSocket")
            val jsonData = Json.encodeToString(prospect)
            println("📤 Envoi des données : $jsonData")
            webSocket.send(jsonData)
            println("⏳ Attente de la réponse...")

            thread {
                val result = webSocket.waitForResult()
                println("📥 Réponse reçue : $result")
                onResult(result)
                Thread.sleep(500)
                println("🔗 Déconnexion...")
                webSocket.close()
            }
            println("🔗 Déconnexion...")
        }
        else {
            println("❌ Connexion impossible")
            onResult("⚠ Impossible de se connecter au serveur WebSocket.")
        }
    }
    catch (e: Exception) {
        println("❌ Message : ${e.message}, StackTrace : ${e.stackTrace}, Cause : ${e.cause}")
        onResult("⚠ Erreur WebSocket.")
    }
}