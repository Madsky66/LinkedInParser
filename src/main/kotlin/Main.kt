import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


import kotlinx.serialization.*
import kotlinx.serialization.json.*
import ui.composable.App
import ui.composable.checkResults
import java.io.File
import java.lang.ProcessBuilder

@Serializable
data class ProspectData(val linkedinURL: String, val name: String = "", val email: String = "", val status: String = "pending")

fun writeJson(data: ProspectData, filePath: String) {
    val jsonData = Json.encodeToString(data)
    File(filePath).writeText(jsonData)
}

fun readJson(filePath: String): ProspectData {
    return try {
        val fileContent = File(filePath).readText()
        Json.decodeFromString(fileContent)
    } catch (e: Exception) {
        println("❌ Erreur lors de la lecture du fichier JSON : ${e.message}")
        ProspectData(linkedinURL = "", status = "error")
    }
}

fun sendToPython() {
    val filePath = "src/main/data/data.json"

    val newProspect = ProspectData(linkedinURL = "https://linkedin.com/in/johndoe")
    if (newProspect.linkedinURL.isBlank()) {println("❌ URL invalide, envoi annulé."); return}
    writeJson(newProspect, filePath)

    val process = ProcessBuilder("python", "src/main/python/process.py").start()
    process.waitFor()

    checkResults(filePath)
    println("✅ Données envoyées à Python : $newProspect")
}



fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}