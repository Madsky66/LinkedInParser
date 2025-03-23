import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.ProspectData
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.serialization.json.Json
import manager.GoogleSheetsManager
import manager.sendToPythonOverWebSocket
import manager.startProfileMonitoring
import ui.composable.ProspectCard

@Composable
fun App() {
    var currentProfile by remember { mutableStateOf<ProspectData?>(null) }
    var isMonitoring by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    MaterialTheme {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colors.background)
        ) {
            // Bouton pour démarrer/arrêter le monitoring
            Button(
                onClick = {
                    if (!isMonitoring) {
                        isMonitoring = true
                        startProfileMonitoring { result ->
                            try {
                                val profile = Json.decodeFromString<ProspectData>(result)
                                if (profile.fullName != "Nom Inconnu") {
                                    currentProfile = profile
                                    errorMessage = ""
                                }
                            } catch (e: Exception) {
                                errorMessage = "⚠ Erreur: ${e.message}"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isMonitoring) "Arrêter le monitoring" else "Démarrer le monitoring")
            }

            // Affichage du profil actuel
            currentProfile?.let { profile ->
                ProspectCard(profile)

                // Bouton pour sauvegarder dans Google Sheets
                Button(
                    onClick = {
                        try {
                            GoogleSheetsManager().saveProspect(profile)
                            errorMessage = "✅ Profil sauvegardé"
                        } catch (e: Exception) {
                            errorMessage = "⚠ Erreur lors de la sauvegarde: ${e.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Sauvegarder le profil")
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    errorMessage,
                    color = if (errorMessage.startsWith("✅")) MaterialTheme.colors.primary
                    else MaterialTheme.colors.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}