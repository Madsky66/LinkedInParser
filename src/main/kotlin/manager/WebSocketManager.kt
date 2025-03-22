package manager

import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.client.WebSocketClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.net.URI
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import data.ProspectData
import java.util.concurrent.TimeUnit

class WebSocketManager(uri: URI, private val onResult: (String) -> Unit) : WebSocketClient(uri) {
    private val latch = CountDownLatch(1)
    var result: String = "En attente..."

    override fun onOpen(handshakedata: ServerHandshake?) {println("✅ Connecté au serveur WebSocket")}

    override fun onMessage(message: String?) {
        message?.let {
            println("📥 Message reçu : $it")
            try {
                val data = Json.decodeFromString<ProspectData>(it)
                result = "✅ Nom complet : ${data.fullName} | Mail : ${data.email}"
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
        println("🔗 Tentative de connexion WebSocket...")
        val connected = webSocket.connectBlocking(5, TimeUnit.SECONDS)

        if (connected && webSocket.isOpen) {
            println("✅ Connexion établie avec le serveur WebSocket")
            val jsonData = Json.encodeToString(prospect)
            println("📤 Envoi des données : $jsonData")
            webSocket.send(jsonData)

            thread {
                try {
                    val result = webSocket.waitForResult()
                    println("📥 Réponse reçue : $result")
                    onResult(result)
                }
                finally {webSocket.close()}
            }
        }
        else {
            println("❌ Connexion impossible - Vérifiez que le serveur Python est bien démarré")
            onResult("⚠ Impossible de se connecter au serveur. Vérifiez que le serveur Python est démarré.")
        }
    }
    catch (e: Exception) {
        println("❌ Erreur WebSocket : ${e.message}")
        e.printStackTrace()
        onResult("⚠ Erreur de connexion au serveur WebSocket.")
    }
}