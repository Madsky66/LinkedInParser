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
        if (message != null) {
            println("📥 Message reçu de Python : $message")
            val data = Json.decodeFromString<ProspectData>(message)
            result = "✅ Nom : ${data.name}, Email : ${data.email}"
            latch.countDown()
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("❌ WebSocket fermé : $reason")
        println("🔗 Déconnexion du serveur WebSocket...")
        latch.countDown()
    }

    override fun onError(ex: Exception?) {
        println("⚠ Erreur WebSocket : ${ex?.message}")
        latch.countDown()
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
        val jsonData = Json.encodeToString(prospect)
        println("📤 Données à envoyer : $jsonData")

        webSocket.connectBlocking()
        println("Websocket => ${webSocket.uri}")
        println("Websocket => ${webSocket.isOpen}")
        println("Websocket => ${webSocket.isClosing}")
        println("🔗 Connexion établie avec succès.")
        webSocket.send(jsonData)
        println("📤 Données envoyées : $jsonData")

        thread {
            val result = webSocket.waitForResult()
            println("📤 Résultat reçu : $result")
            onResult(result)
            println("🔗 Déconnexion du serveur WebSocket...")
        }
    }
    catch (e: IllegalArgumentException) {
        println("❌ Erreur WebSocket : ${e.message}")
        onResult("⚠ URL WebSocket invalide.")
    }
    catch (e: InterruptedException) {
        println("❌ Erreur WebSocket : ${e.message}")
        onResult("⚠ Connexion au serveur WebSocket interrompue.")
    }
    catch (e: IllegalStateException) {
        println("❌ Erreur WebSocket : ${e.message}")
        onResult("⚠ Connexion au serveur WebSocket déjà établie.")
    }
    catch (e: NullPointerException) {
        println("❌ Erreur WebSocket : ${e.message}")
        onResult("⚠ Connexion au serveur WebSocket impossible.")
    }
    catch (e: org.java_websocket.exceptions.WebsocketNotConnectedException) {
        println("❌ Erreur WebSocket : ${e.message}")
        onResult("⚠ Connexion au serveur WebSocket impossible.")
    }
    catch (e: Exception) {
        println("❌ Erreur WebSocket : ${e.message}")
        onResult("⚠ Erreur de connexion au serveur WebSocket.")
    }
    catch (e: Error) {
        println("❌ Erreur WebSocket : ${e.message}")
        onResult("⚠ Erreur de connexion au serveur WebSocket.")
    }
    catch (e: Throwable) {
        println("❌ Erreur WebSocket : ${e.message}")
        onResult("⚠ Erreur de connexion au serveur WebSocket.")
    }
}
