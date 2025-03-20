import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI

@Serializable
data class ProspectData(val linkedinURL: String, val name: String = "", val email: String = "", val status: String = "pending")

fun writeJson(data: ProspectData, filePath: String) {
    try {
        val jsonData = Json.encodeToString(data)
        File(filePath).writeText(jsonData)
    }
    catch (e: Exception) {println("❌ Erreur lors de l'écriture JSON : ${e.message}")}
}

fun readJson(filePath: String): ProspectData {
    return try {
        val fileContent = File(filePath).readText()
        Json.decodeFromString(fileContent)
    }
    catch (e: Exception) {
        println("❌ Erreur lors de la lecture du fichier JSON : ${e.message}")
        ProspectData(linkedinURL = "", status = "error")
    }
}

fun sendToPython() {
    val filePath = "src/main/data/data.json"
    val newProspect = ProspectData(linkedinURL = "https://linkedin.com/in/johndoe")
    if (newProspect.linkedinURL.isBlank()) {println("❌ URL invalide, envoi annulé."); return}
    writeJson(newProspect, filePath)

    try {
        val process = ProcessBuilder("python", "src/main/python/process.py").start()
        process.waitFor()
        val result = checkResults(filePath)
        println("✅ Résultat du traitement : $result")
    }
    catch (e: Exception) {println("❌ Erreur lors de l'exécution du script Python : ${e.message}")}
}

fun checkResults(filePath: String): String {
    val processedData = readJson(filePath)
    return if (processedData.status == "completed") {"✅ Nom : ${processedData.name}, Email : ${processedData.email}"} else {"⏳ En attente du traitement Python..."}
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}