package ui.composable

import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import data.ProspectData
import kotlinx.serialization.json.Json
import manager.GoogleSheetsManager
import manager.WebSocketManager
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Dimension

@Composable
fun App() {
    var urlInput by remember {mutableStateOf("")}
    var statusMessage by remember {mutableStateOf("En attente de connexion...")}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var isLoading by remember {mutableStateOf(false)}

    // Initialiser JavaFX WebView
    val jfxPanel = remember {JFXPanel()}
    var webView by remember {mutableStateOf<WebView?>(null)}

    LaunchedEffect(Unit) {
        Platform.runLater {
            val newWebView = WebView()
            val scene = Scene(newWebView)
            jfxPanel.scene = scene
            webView = newWebView
            newWebView.engine.load("https://www.linkedin.com/login")
        }
    }

    LaunchedEffect(Unit) {
        WebSocketManager.initialize {result ->
            try {
                val profile = Json.decodeFromString<ProspectData>(result)
                currentProfile = profile
                statusMessage = "✅ Profil mis à jour"
                isLoading = false
            }
            catch (e: Exception) {
                statusMessage = "❌ Erreur: ${e.message}"
                isLoading = false
            }
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
                            isLoading = true
                            WebSocketManager.sendProfileRequest(urlInput)
                            Platform.runLater {webView?.engine?.load(urlInput)}
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
                if (isLoading) {CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))}
                SwingPanel(
                    modifier = Modifier.fillMaxSize(),
                    factory = {JPanel(BorderLayout()).apply {preferredSize = Dimension(800, 600); add(jfxPanel, BorderLayout.CENTER)}}
                )
            }
        }
    }
}