import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import data.ProspectData
import data.ProspectStatus
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import manager.JavaFxManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import ui.composable.EmptyProspectCard
import ui.composable.ProspectCard
import java.awt.BorderLayout
import java.awt.Dimension
import java.net.MalformedURLException
import javax.swing.JPanel
import kotlin.text.contains


@Composable
fun MainContent(windowState: WindowState, applicationScope: CoroutineScope, googleSheetsManager: GoogleSheetsManager, prospectList: MutableList<ProspectData>) {
    var urlInput by remember {mutableStateOf("")}
    var isLoggedInToLinkedIn by remember {mutableStateOf(false)}
    var statusMessage by remember {mutableStateOf("En attente de connexion...")}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var isLoading by remember {mutableStateOf(false)}
    var webViewReady by remember {mutableStateOf(false)}
    val logger = LoggerFactory.getLogger("App")

    val jfxPanel = remember {JFXPanel()}
    var webView by remember {mutableStateOf<WebView?>(null)}

    var spreadsheetId by remember {mutableStateOf("")}
    var newProspect by remember {mutableStateOf(ProspectData())}

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
        Column(Modifier.fillMaxSize().background(Color.Gray).padding(10.dp)) {
            // Zone du navigateur
            Box(Modifier.weight(2f).fillMaxSize(), Alignment.Center) {
                if (!webViewReady) CircularProgressIndicator(Modifier.align(Alignment.Center))
                SwingPanel(
                    modifier = Modifier.fillMaxSize(),
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
            Spacer(Modifier.height(10.dp))
            // ZONE DU BAS
            Row(Modifier.weight(1f).background(Color.DarkGray).padding(10.dp)) {
                Column(Modifier.weight(2f).fillMaxHeight(), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
                    // Statut
                    Text(
                        statusMessage, Modifier.padding(8.dp), color = when {
                            statusMessage.startsWith("✅") -> Color.Green
                            statusMessage.startsWith("❌") -> Color.Red
                            else -> Color.White
                        }
                    )
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
                                                    ProspectStatus.COMPLETED -> "✅ Profil récupéré avec succès"
                                                    ProspectStatus.ERROR -> "❌ Erreur: ${currentProfile?.error ?: "Inconnue"}"
                                                    ProspectStatus.IN_PROGRESS -> "IN_PROGRESS"
                                                    ProspectStatus.PENDING -> "PENDING"
                                                    else -> "⚠️ Statut inattendu: ${currentProfile?.status}"
                                                }
                                            if (currentProfile?.status == ProspectStatus.COMPLETED) {
                                                currentProfile?.let {
//                                                    googleSheetsManager.writeProspectToSheet(spreadsheetId, newProspect)
//                                                    prospectList.clear()
//                                                    prospectList.addAll(googleSheetsManager.readProspectsFromSheet(spreadsheetId))
//                                                    newProspect = ProspectData()
                                                }
                                            }
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
                }
                Spacer(Modifier.width(10.dp))
                // Fiche contact
                Column(Modifier.weight(1f).fillMaxSize()) {
                    if (isLoading) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                    else currentProfile?.let {ProspectCard(it)} ?: EmptyProspectCard()
                }
            }
        }
    }
}

fun isValidLinkedInURL(url: String): Boolean =
    try {url.startsWith("https://www.linkedin.com/in/") || url.startsWith("https://linkedin.com/in/")}
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
            status = ProspectStatus.COMPLETED,
            fullName = fullName,
            firstName = firstName,
            lastName = lastName,
            email = email,
            company = company,
            position = position
        )
    }
    catch (e: Exception) {ProspectData(linkedinURL = url, status = ProspectStatus.ERROR, error = e.message ?: "Unknown error")}
}