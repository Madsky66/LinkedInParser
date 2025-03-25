package ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import data.ProspectData
import kotlinx.serialization.json.Json
import manager.WebSocketManager
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Dimension

private val DarkThemeColors = darkColors(
    primary = Color(0xFF2196F3),
    primaryVariant = Color(0xFF1976D2),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun App(windowState: WindowState) {
    var urlInput by remember {mutableStateOf("")}
    var isLoggedInToLinkedIn by remember {mutableStateOf(false)}
    var statusMessage by remember {mutableStateOf("En attente de connexion...")}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var isLoading by remember {mutableStateOf(false)}
    var webViewReady by remember {mutableStateOf(false)}
    val coroutineScope = rememberCoroutineScope()

    val jfxPanel = remember {JFXPanel()}
    var webView by remember {mutableStateOf<WebView?>(null)}
    var webSocketConnected by remember {mutableStateOf(false)}

    LaunchedEffect(Unit) {
        Platform.runLater {
            try {
                val newWebView = WebView()
                val scene = Scene(newWebView)
                jfxPanel.scene = scene
                webView = newWebView
                webView?.apply {
                    engine.userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                    engine.load("https://www.linkedin.com/login")
                    engine.locationProperty().addListener {_, oldLocation, newLocation ->
                        if (newLocation != null) {
                            Platform.runLater {
                                if (oldLocation?.contains("linkedin.com/login") == true &&
                                    (newLocation.contains("linkedin.com/feed") || newLocation.contains("linkedin.com/home"))) {
                                    isLoggedInToLinkedIn = true
                                    statusMessage = "✅ Connecté à LinkedIn"
                                    webView?.engine?.load(newLocation)
                                }
                            }
                        }
                    }
                    engine.loadWorker.stateProperty().addListener {_, _, newState -> if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {Platform.runLater {jfxPanel.isVisible = true}}}
                }
                webViewReady = true
                println("✅ WebView initialisée avec succès")
            }
            catch (e: Exception) {
                println("❌ Erreur lors de l'initialisation de la WebView : ${e.message}")
                webViewReady = false
            }
        }
    }

    MaterialTheme(colors = DarkThemeColors) {
        Row(Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
            // Panneau latéral gauche
            Column(Modifier.width(400.dp).fillMaxHeight().background(MaterialTheme.colors.surface).padding(16.dp)) {
                // Zone de recherche
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {urlInput = it},
                    label = {Text("URL du profil LinkedIn")},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isLoggedInToLinkedIn,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(0.12f),
                        disabledTextColor = MaterialTheme.colors.onSurface.copy(0.6f),
                        disabledBorderColor = MaterialTheme.colors.onSurface.copy(0.12f),
                        disabledLabelColor = MaterialTheme.colors.onSurface.copy(0.4f)
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (urlInput.isNotBlank() && isLoggedInToLinkedIn) {
                                    currentProfile = null
                                    statusMessage = "⏳ Analyse du profil en cours..."
                                    isLoading = true
                                    Platform.runLater {webView?.engine?.load(urlInput)}
                                    if (webSocketConnected) {WebSocketManager.sendProfileRequest(urlInput)}
                                    else {
                                        coroutineScope.launch {
                                            try {
                                                WebSocketManager.initialize {resultJson ->
                                                    coroutineScope.launch {
                                                        try {
                                                            val result = Json.decodeFromString<ProspectData>(resultJson)
                                                            Platform.runLater {
                                                                currentProfile = result
                                                                isLoading = false
                                                                statusMessage =
                                                                    when (result.status) {
                                                                        "completed" -> "✅ Profil récupéré avec succès"
                                                                        "error" -> "❌ Erreur: ${result.error ?: "Inconnue"}"
                                                                        else -> "⚠️ Statut inattendu: ${result.status}"
                                                                    }
                                                            }
                                                        }
                                                        catch (e: Exception) {
                                                            isLoading = false
                                                            statusMessage = "❌ Erreur de traitement des données: ${e.message}"
                                                        }
                                                    }
                                                }
                                                WebSocketManager.sendProfileRequest(urlInput)
                                                webSocketConnected = true
                                            }
                                            catch (e: Exception) {
                                                isLoading = false
                                                statusMessage = "❌ Impossible de se connecter au serveur: ${e.message}"
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = urlInput.isNotBlank() && isLoggedInToLinkedIn
                        ) {
                            Icon(Icons.Default.Search, "Rechercher", tint = if (urlInput.isNotBlank() && isLoggedInToLinkedIn) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(0.4f))
                        }
                    }
                )
                // Statut
                if (statusMessage.isNotEmpty()) {
                    Text(
                        statusMessage,
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = when {
                            statusMessage.startsWith("✅") -> Color.Green
                            statusMessage.startsWith("❌") -> Color.Red
                            else -> MaterialTheme.colors.onSurface
                        }
                    )
                }
                // Fiche contact
                Box(Modifier.fillMaxWidth().weight(1f).padding(vertical = 16.dp)) {
                    if (isLoading) {CircularProgressIndicator(Modifier.align(Alignment.Center))}
                    else {
                        if (currentProfile != null) {ProspectCard(currentProfile!!)}
                        else {EmptyProspectCard()}
                    }
                }
            }
            // Zone du navigateur
            Box(Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colors.background)) {
                if (!webViewReady) {CircularProgressIndicator(Modifier.align(Alignment.Center))}
                SwingPanel(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        JPanel(BorderLayout()).apply {
                            preferredSize = Dimension(windowState.size.width.value.toInt() - 400, windowState.size.height.value.toInt())
                            add(jfxPanel, BorderLayout.CENTER)
                            isOpaque = true
                            background = java.awt.Color.WHITE
                            isVisible = true
                        }
                    },
                    update = {panel ->
                        panel.preferredSize = Dimension(windowState.size.width.value.toInt() - 400, windowState.size.height.value.toInt())
                        panel.revalidate()
                        panel.repaint()
                    }
                )
            }
        }
    }
}