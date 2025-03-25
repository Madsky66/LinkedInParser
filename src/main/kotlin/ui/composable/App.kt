package ui.composable

import DrawerMenu
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import kotlinx.coroutines.*
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Dimension
import java.net.MalformedURLException
import java.net.URL
import manager.GoogleSheetsManager
import manager.JavaFxManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory

@Composable
fun ContactCard() {
    Card(Modifier.fillMaxHeight()) {
        Text("Détails du contact")
    }
}

@Composable
fun App(windowState: WindowState, applicationScope: CoroutineScope) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalDrawer(drawerState = drawerState, drawerContent = {DrawerMenu()}) {
        TopAppBar(
            title = {Text("Mon Application")},
            navigationIcon = {
                IconButton(onClick = {
                    coroutineScope.launch {
                        if (drawerState.isOpen) {drawerState.close()}
                        else {drawerState.open()}
                    }
                }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
            }
        )
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(2f)) {MainContent(windowState, applicationScope)}
            Column(Modifier.weight(1f).fillMaxHeight()) {ContactCard()}
        }
    }
}

@Composable
fun MainContent(windowState: WindowState, applicationScope: CoroutineScope) {
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
        JavaFxManager.initialize()
        delay(500)
        Platform.runLater {
            try {
                val localWebView = WebView()
                localWebView.engine.userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                jfxPanel.scene = Scene(localWebView)
                webView = localWebView
                webView?.apply {
//                    engine.load("https://www.linkedin.com/login")
                    engine.javaScriptEnabledProperty()
                    engine.load("https://www.google.com")
                    engine.locationProperty().addListener {_, _, newLocation ->
                        if (newLocation != null) {
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

    MaterialTheme(colors = darkColors()) {
        Column(Modifier.fillMaxSize().background(Color.LightGray)) {
            // Zone du navigateur
            Box(Modifier.weight(2f).fillMaxSize().background(Color.LightGray)) {
                if (!webViewReady) CircularProgressIndicator(Modifier.align(Alignment.Center))
                SwingPanel(
                    modifier = Modifier.fillMaxSize().background(Color.LightGray),
                    factory = {
                        JPanel(BorderLayout()).apply {
                            background = java.awt.Color.LIGHT_GRAY
                            preferredSize = Dimension(windowState.size.width.value.toInt() - 400, windowState.size.height.value.toInt())
                            add(jfxPanel, BorderLayout.CENTER)
                            isOpaque = true
                            isVisible = true
                        }
                    },
                    update = {it.revalidate(); it.repaint()}
                )
            }

            // ZONE DU BAS (TEST)
            Row(Modifier.weight(1f).fillMaxHeight().padding(5.dp).background(Color.DarkGray).padding(10.dp)) {
                Column(Modifier.weight(2f).fillMaxHeight()) {
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
                                    if (isValidLinkedInURL(urlInput)) {
                                        currentProfile = null
                                        statusMessage = "⏳ Analyse en cours..."
                                        isLoading = true
                                        if (webViewReady) {Platform.runLater {webView?.engine?.load(urlInput)}}
                                        else {statusMessage = "🚨 WebView en cours d'initialisation, veuillez patienter..."}
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
                                    else {statusMessage = "❌ URL invalide"}
                                },
                                enabled = isLoggedInToLinkedIn
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
                    Text(
                        statusMessage, Modifier.padding(8.dp), color = when {
                            statusMessage.startsWith("✅") -> Color.Green
                            statusMessage.startsWith("❌") -> Color.Red
                            else -> Color.White
                        }
                    )
                }
                Spacer(Modifier.width(10.dp))
                // Fiche contact
                Box(Modifier.weight(1f).fillMaxSize()) {
                    if (isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center))
                    else currentProfile?.let {ProspectCard(it)} ?: EmptyProspectCard()
                }
            }
        }
    }
}

fun isValidLinkedInURL(url: String): Boolean =
    try {
        val parsedURL = URL(url)
        parsedURL.host.contains("linkedin.com") && url.startsWith("https://www.linkedin.com/in/")
    }
    catch (e: MalformedURLException) {false}

suspend fun scrapeLinkedInProfile(url: String): ProspectData? = withContext(Dispatchers.IO) {
    try {
        val document: Document = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate").timeout(10000).get()
        val fullName = document.selectFirst("h1")?.text()?.trim() ?: ""
        val names = fullName.split(' ', limit = 2)
        val firstName = names.getOrNull(0) ?: ""
        val lastName = names.getOrNull(1) ?: ""
        val position = document.selectFirst("div.text-body-medium.break-words")?.text()?.trim() ?: ""
        val company = document.selectFirst("ul.oMCvYEyXcrlxrHfavlrlScyeoqjyWzYwlds li button span")?.text()?.trim() ?: ""
        val email = document.select("a[href^=mailto]").first()?.attr("href")?.replace("mailto:", "") ?: ""

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