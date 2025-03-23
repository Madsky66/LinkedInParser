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
            println("📥 Message brut reçu : $it")
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

    fun requestCurrentProfile() {
        if (this.isOpen) {
            // Envoie une requête spéciale pour obtenir le profil actuel
            val request = Json.encodeToString(ProspectData(
                linkedinURL = "",
                status = "request_current"
            ))
            this.send(request)
        }
    }
}

fun startProfileMonitoring(onResult: (String) -> Unit) {
    try {
        val webSocket = WebSocketManager(URI("ws://localhost:9000"), onResult)
        println("🔗 Démarrage du monitoring du profil...")

        if (webSocket.connectBlocking(5, TimeUnit.SECONDS)) {
            // Démarre un thread pour le monitoring
            thread {
                while (webSocket.isOpen) {
                    webSocket.requestCurrentProfile()
                    Thread.sleep(2000) // Vérifie toutes les 2 secondes
                }
            }
        }
    } catch (e: Exception) {
        println("❌ Erreur de connexion WebSocket: ${e.message}")
        onResult("⚠ Erreur de connexion au serveur WebSocket.")
    }
}