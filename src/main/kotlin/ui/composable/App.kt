import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import data.ProspectData
import kotlinx.serialization.json.Json
import manager.GoogleSheetsManager
import manager.WebSocketManager
import ui.composable.ProspectCard
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component

@Composable
fun App() {
    var urlInput by remember {mutableStateOf("")}
    var statusMessage by remember {mutableStateOf("En attente de connexion...")}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var chromePanel by remember {mutableStateOf<Component?>(null)}

    LaunchedEffect(Unit) {
        WebSocketManager.initialize {result ->
            try {
                val profile = Json.decodeFromString<ProspectData>(result)
                currentProfile = profile
                statusMessage = "✅ Profil mis à jour"
            }
            catch (e: Exception) {statusMessage = "❌ Erreur: ${e.message}"}
        }
    }

    MaterialTheme {
        Row(Modifier.fillMaxSize()) {
            // Partie gauche (1/3 de l'écran)
            Column(Modifier.weight(1f).padding(16.dp).fillMaxHeight()) {
                Text("LinkedIn Parser Pro", Modifier.padding(bottom = 16.dp), style = MaterialTheme.typography.h5,)
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {urlInput = it},
                    label = {Text("URL du profil LinkedIn")},
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            currentProfile = null
                            statusMessage = "⏳ Analyse du profil en cours..."
                            WebSocketManager.sendProfileRequest(urlInput)
                        }
                        else {statusMessage = "⚠️ Veuillez entrer une URL LinkedIn valide"}
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    enabled = urlInput.isNotBlank()
                ) {
                    Text("Analyser le profil")
                }
                // Statut actuel
                Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {Text(statusMessage, Modifier.padding(16.dp))}
                // Affichage du profil actuel
                currentProfile?.let {profile ->
                    ProspectCard(profile)
                    Button(
                        onClick = {
                            try {
                                GoogleSheetsManager().saveProspect(profile)
                                statusMessage = "✅ Profil sauvegardé dans Google Sheets"
                            }
                            catch (e: Exception) {statusMessage = "❌ Erreur: ${e.message}"}
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Exporter vers Google Sheets")
                    }
                }
            }
            // Partie droite (2/3 de l'écran) - Zone du navigateur
            Box(Modifier.weight(2f).fillMaxHeight().background(MaterialTheme.colors.surface)) {
                SwingPanel(
                    modifier = Modifier.fillMaxSize(),
                    factory = {JPanel(BorderLayout()).apply {background = Color.WHITE}}
                )
            }
        }
    }
}