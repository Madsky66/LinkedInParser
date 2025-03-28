package ui.composable

import GoogleSheetsManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import manager.LinkedInManager
import org.slf4j.LoggerFactory
import ui.theme.Colors
import java.awt.Desktop
import java.net.URI

@Composable
fun App(applicationScope: CoroutineScope) {
    var pastedInput by remember {mutableStateOf("")}
    var pastedURL by remember {mutableStateOf("")}
    var statusMessage by remember {mutableStateOf("En attente de données...")}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var isLoading by remember {mutableStateOf(false)}
    val logger = LoggerFactory.getLogger("App")

    val linkedInManager = LinkedInManager()
    val googleSheetsManager = remember {GoogleSheetsManager()}
//    val prospectList = remember {mutableStateListOf<ProspectData>()}
//    var spreadsheetId by remember {mutableStateOf("")}
//    var newProspect by remember {mutableStateOf(ProspectData())}

    MaterialTheme(colors = darkColors()) {
        Column(Modifier.fillMaxSize().background(Color.DarkGray).padding(10.dp)) {
            Row(Modifier.weight(0.9f).fillMaxWidth()) {
                // Zone de texte
                Column(Modifier.weight(2f).fillMaxHeight(), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = pastedInput,
                        onValueChange = {
                            pastedInput = it
                            applicationScope.launch {
                                isLoading = true
                                statusMessage = "⏳ Extraction des données en cours..."
                                try {
                                    currentProfile = linkedInManager.extractProfileData(pastedInput)
                                    statusMessage = "✅ Extraction réussie"
                                }
                                catch (e: Exception) {
                                    statusMessage = "❌ Erreur lors de l'extraction : ${e.message}"
                                    logger.error("Erreur extraction LinkedIn", e)
                                }
                                isLoading = false
                            }
                        },
                        label = {Text("Coller le texte de la page LinkedIn ici...")},
                        modifier = Modifier.weight(0.9f).fillMaxWidth().clip(RectangleShape),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.LightGray,
                            focusedBorderColor = Color.LightGray.copy(0.25f),
                            unfocusedBorderColor = Color.LightGray.copy(0.15f),
                            focusedLabelColor = Color.LightGray.copy(0.5f),
                            unfocusedLabelColor = Color.LightGray.copy(0.5f),
                            placeholderColor = Color.LightGray.copy(0.25f)
                        )
                    )
                }

                // Spacer
                Spacer(Modifier.width(10.dp))

                // Colonne de droite
                Column(Modifier.weight(1f).fillMaxHeight().padding(5.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                    Column(Modifier.fillMaxWidth()) {
                        // Fiche contact
                        if (isLoading) {CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))}
                        else {currentProfile?.let {ProspectCard(it)} ?: EmptyProspectCard()}

                        // Spaced Divider
                        Spacer(Modifier.height(15.dp))
                        Divider(Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                    }

                    // Boutons
                    Column(Modifier.fillMaxSize(), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                        Column(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = pastedURL,
                                onValueChange = {pastedURL = it},
                                label = {Text("Coller l'URL de la page LinkedIn ici...")},
                                modifier = Modifier.fillMaxWidth().clip(RectangleShape),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = Color.LightGray,
                                    focusedBorderColor = Color.LightGray.copy(0.25f),
                                    unfocusedBorderColor = Color.LightGray.copy(0.15f),
                                    focusedLabelColor = Color.LightGray.copy(0.5f),
                                    unfocusedLabelColor = Color.LightGray.copy(0.5f),
                                    placeholderColor = Color.LightGray.copy(0.25f)
                                )
                            )
                            Button(
                                onClick = {
                                    if (pastedURL.isNotEmpty()) {
                                        try {
                                            val uri = URI(pastedURL)
                                            if (Desktop.isDesktopSupported()) {Desktop.getDesktop().browse(uri)}
                                        }
                                        catch (e: Exception) {
                                            statusMessage = "❌ Erreur ouverture URL : ${e.message}"
                                            logger.error("Erreur ouverture URL", e)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = pastedURL.startsWith("https://www.linkedin.com/in") || pastedURL.startsWith("https://linkedin.com/in"),
                                elevation = ButtonDefaults.elevation(10.dp),
                                shape = RoundedCornerShape(0, 0, 50, 50),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.DarkGray,
                                    contentColor = Color.LightGray,
                                    disabledBackgroundColor = Colors().DARK_GRAY,
                                    disabledContentColor = Color.DarkGray,
                                )
                            ) {
                                Text("Ouvrir le profil")
                            }

                            // Spaced Divider
                            Spacer(Modifier.height(15.dp))
                            Divider(Modifier.fillMaxWidth())
                            Spacer(Modifier.height(15.dp))
                        }

                        Button(
                            onClick = {
                                applicationScope.launch {
                                    isLoading = true
                                    statusMessage = "⏳ Exportation en cours..."
                                    try {
                                        googleSheetsManager.exportToCSV(currentProfile!!, "data_export.csv")
                                        statusMessage = "✅ Exportation réussie"
                                    }
                                    catch (e: Exception) {
                                        statusMessage = "❌ Erreur exportation : ${e.message}"
                                        logger.error("Erreur exportation CSV", e)
                                    }
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = currentProfile?.fullName != "" && currentProfile?.fullName != "Nom inconnu",
                            elevation = ButtonDefaults.elevation(10.dp),
                            shape = RoundedCornerShape(100),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.DarkGray,
                                contentColor = Color.LightGray,
                                disabledBackgroundColor = Colors().DARK_GRAY,
                                disabledContentColor = Color.DarkGray,
                            )
                        ) {
                            Text("Extraire [CSV]")
                        }
                    }
                }
            }

            // Spacer
            Spacer(Modifier.height(10.dp))

            // Console
            Column(Modifier.weight(0.1f).fillMaxWidth().background(Color.Black)) {
                Text(
                    statusMessage, Modifier.padding(20.dp, 10.dp), color = when {
                        statusMessage.startsWith("✅") -> Color.Green
                        statusMessage.startsWith("❌") -> Color.Red
                        else -> Color.LightGray
                    }
                )
            }
        }
    }
}