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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import data.ProspectData
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Dimension
import manager.GoogleSheetsManager
import org.slf4j.LoggerFactory
import java.net.MalformedURLException
import java.net.URL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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
fun App(windowState: WindowState, applicationScope: CoroutineScope) {
    var urlInput by remember {mutableStateOf("")}
    var isLoggedInToLinkedIn by remember {mutableStateOf(false)}
    var statusMessage by remember {mutableStateOf("En attente de connexion...")}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var isLoading by remember {mutableStateOf(false)}
    var webViewReady by remember {mutableStateOf(false)}
    val googleSheetsManager = remember {GoogleSheetsManager()}
    val logger = LoggerFactory.getLogger("App")

    val jfxPanel = remember {JFXPanel()}
    var webView by remember {mutableStateOf<WebView?>(null)}

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
                    engine.locationProperty().addListener {_, _, newLocation ->
                        if (newLocation != null) {
                            logger.info("🔍 Redirection détectée : $newLocation")
                            Platform.runLater {
                                if (newLocation.contains("linkedin.com/feed") || newLocation.contains("linkedin.com/home")) {
                                    isLoggedInToLinkedIn = true
                                    statusMessage = "✅ Connecté à LinkedIn"
                                }
                            }
                        }
                    }
                    engine.loadWorker.stateProperty().addListener {_, _, newState -> if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {Platform.runLater {jfxPanel.isVisible = true}}}
                }
                webViewReady = true
                logger.info("WebView initialisée avec succès")
            }
            catch (e: Exception) {
                logger.error("Erreur lors de l'initialisation de la WebView : ${e.message}", e)
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
                                    if (!isValidLinkedInURL(urlInput)) {
                                        statusMessage = "❌ URL LinkedIn invalide"
                                        return@IconButton
                                    }
                                    currentProfile = null
                                    statusMessage = "⏳ Analyse du profil en cours..."
                                    isLoading = true
                                    Platform.runLater {webView?.engine?.load(urlInput)}
                                    applicationScope.launch {
                                        currentProfile = scrapeLinkedInProfile(urlInput)
                                        isLoading = false
                                        statusMessage =
                                            when (currentProfile?.status) {
                                                "completed" -> "✅ Profil récupéré avec succès"
                                                "error" -> "❌ Erreur: ${currentProfile?.error ?: "Inconnue"}"
                                                else -> "⚠️ Statut inattendu: ${currentProfile?.status}"
                                            }
                                        if (currentProfile?.status == "completed") {currentProfile?.let {googleSheetsManager.saveProspect(it, applicationScope)}}
                                    }
                                }
                            },
                            enabled = urlInput.isNotBlank() && isLoggedInToLinkedIn
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Rechercher",
                                tint =
                                if (urlInput.isNotBlank() && isLoggedInToLinkedIn) MaterialTheme.colors.primary
                                else MaterialTheme.colors.onSurface.copy(0.4f)
                            )
                        }
                    }
                )
                // Statut
                if (statusMessage.isNotEmpty()) {
                    Text(
                        statusMessage, Modifier.padding(vertical = 8.dp),
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

fun isValidLinkedInURL(url: String): Boolean {
    return try {
        val parsedURL = URL(url)
        val host = parsedURL.host
        (host.contains("linkedin.com") || host.contains("www.linkedin.com")) && (url.startsWith("https://www.linkedin.com/in/") || url.startsWith("https://linkedin.com/in/"))
    }
    catch (e: MalformedURLException) {false}
}

suspend fun scrapeLinkedInProfile(url: String): ProspectData? {
    return withContext(Dispatchers.IO) {
        try {
            val document: Document = Jsoup.connect(url).get()
            val fullNameElement = document.selectFirst("h1.text-heading-xlarge")
            val fullName = fullNameElement?.text()?.trim() ?: ""
            val names = fullName.split(' ', limit = 2)
            val firstName = names.getOrNull(0) ?: ""
            val lastName = names.getOrNull(1) ?: ""
            val positionElement = document.selectFirst("div.text-body-medium.break-words")
            val position = positionElement?.text()?.trim() ?: ""
            val companyElement = document.selectFirst("span.text-body-small.inline")
            val company = companyElement?.text()?.trim() ?: ""
            val email = ""

            ProspectData(
                linkedinURL = url,
                status = "completed",
                fullName = fullName,
                firstName = firstName,
                lastName = lastName,
                email = email,
                company = company,
                position = position
            )
        }
        catch (e: Exception) {ProspectData(linkedinURL = url, status = "error", error = e.message ?: "Unknown error")}
    }
}