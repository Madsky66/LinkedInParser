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
import androidx.compose.ui.unit.sp
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import manager.LinkedInManager
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.net.URI

@Composable
fun App(applicationScope: CoroutineScope, COLOR_PRIMARY: Color, COLOR_NEUTRAL: Color, COLOR_SECONDARY: Color) {
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

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(COLOR_NEUTRAL).padding(20.dp, 15.dp, 20.dp, 20.dp)) {
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
                            textColor = COLOR_SECONDARY,
                            focusedBorderColor = COLOR_SECONDARY.copy(0.25f),
                            unfocusedBorderColor = COLOR_SECONDARY.copy(0.15f),
                            focusedLabelColor = COLOR_SECONDARY.copy(0.5f),
                            unfocusedLabelColor = COLOR_SECONDARY.copy(0.5f),
                            placeholderColor = COLOR_SECONDARY.copy(0.25f)
                        )
                    )
                }

                // Spacer
                Spacer(Modifier.width(15.dp))

                // Colonne de droite
                Column(Modifier.weight(1f).fillMaxHeight().padding(5.dp, 5.dp, 0.dp, 0.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                    Column(Modifier.fillMaxWidth()) {
                        // Fiche contact
                        if (isLoading) {CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))}
                        else {currentProfile?.let {ProspectCard(it, COLOR_PRIMARY, COLOR_SECONDARY)} ?: EmptyProspectCard(COLOR_PRIMARY, COLOR_SECONDARY)}

                        // Spaced Divider
                        Spacer(Modifier.height(15.dp))
                        Divider(Modifier.fillMaxWidth().padding(50.dp, 0.dp), color = COLOR_SECONDARY.copy(0.05f))
                        Spacer(Modifier.height(10.dp))
                    }

                    // Options
                    Column(Modifier.fillMaxSize(), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                        Column(Modifier.fillMaxWidth()) {
                            // Input
                            OutlinedTextField(
                                value = pastedURL,
                                onValueChange = {pastedURL = it},
                                label = {Text("Coller l'URL de la page LinkedIn ici...")},
                                modifier = Modifier.fillMaxWidth().clip(RectangleShape),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = COLOR_SECONDARY,
                                    focusedBorderColor = COLOR_SECONDARY.copy(0.25f),
                                    unfocusedBorderColor = COLOR_SECONDARY.copy(0.15f),
                                    focusedLabelColor = COLOR_SECONDARY.copy(0.5f),
                                    unfocusedLabelColor = COLOR_SECONDARY.copy(0.5f),
                                    placeholderColor = COLOR_SECONDARY.copy(0.25f)
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
                                    backgroundColor = COLOR_NEUTRAL,
                                    contentColor = COLOR_SECONDARY,
                                    disabledBackgroundColor = COLOR_PRIMARY,
                                    disabledContentColor = COLOR_NEUTRAL,
                                )
                            ) {
                                Text("Ouvrir le profil")
                            }

                            // Spaced Divider
                            Spacer(Modifier.height(15.dp))
                            Divider(Modifier.fillMaxWidth().padding(50.dp, 0.dp), color = COLOR_SECONDARY.copy(0.05f))
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
                                backgroundColor = COLOR_NEUTRAL,
                                contentColor = COLOR_SECONDARY,
                                disabledBackgroundColor = COLOR_PRIMARY,
                                disabledContentColor = COLOR_NEUTRAL,
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
            Column(Modifier.weight(0.1f).fillMaxWidth().background(Color.Black), Arrangement.Center) {
                Text(
                    statusMessage, Modifier.padding(20.dp, 10.dp), fontSize = 15.sp, color = when {
                        statusMessage.startsWith("✅") -> Color.Green
                        statusMessage.startsWith("❌") -> Color.Red
                        else -> COLOR_SECONDARY
                    }
                )
            }
        }
    }
}