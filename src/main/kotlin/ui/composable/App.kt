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
import java.awt.Desktop
import java.net.URI

@Composable
fun App(applicationScope: CoroutineScope, COLOR_PRIMARY: Color, COLOR_NEUTRAL: Color, COLOR_SECONDARY: Color) {
    var pastedInput by remember {mutableStateOf("")}
    var pastedURL by remember {mutableStateOf("")}
    var pastedAPI by remember {mutableStateOf("")}

    var statusMessage by remember {mutableStateOf("En attente de données...")}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var isLoading by remember {mutableStateOf(false)}

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
                            if (pastedInput == "") {applicationScope.launch {statusMessage = "En attente de données..."}}
                            else {
                                applicationScope.launch {
                                    isLoading = true
                                    statusMessage = "⏳ Extraction des informations en cours..."
                                    try {currentProfile = linkedInManager.extractProfileData(pastedInput)}
                                    catch (e: Exception) {statusMessage = "❌ Erreur lors de l'extraction des informations : ${e.message}"}
                                    statusMessage =
                                        if (currentProfile?.fullName == "") {"❌ Aucune information extraite"}
                                        else if (currentProfile?.fullName != "Prénom iconnu Nom de famille inconnu") {"❌ Erreur lors de l'extraction des informations"}
                                        else if (currentProfile?.firstName == "Prénom inconnu" || currentProfile?.lastName == "Nom de famille inconnu") {"⚠\uFE0F Extraction des données incomplète"}
                                        else {"✅ Extraction des informations réussie"}
                                    isLoading = false
                                }
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
                                        catch (e: Exception) {statusMessage = "❌ Erreur lors de l'ouverture de l'URL : ${e.message}"}
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = pastedURL.startsWith("https://www.linkedin.com/in")
                                        || pastedURL.startsWith("https://linkedin.com/in")
                                        || pastedURL.startsWith("http://www.linkedin.com/in")
                                        || pastedURL.startsWith("http://linkedin.com/in"),
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

                        // Bouton d'extraction CSV
                        Column(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth()){
                                OutlinedTextField(
                                    value = pastedAPI,
                                    onValueChange = {pastedAPI = it},
                                    label = {Text("Clé API Apollo...")},
                                    modifier = Modifier.fillMaxWidth(0.5f).clip(RectangleShape),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        textColor = COLOR_SECONDARY,
                                        focusedBorderColor = COLOR_SECONDARY.copy(0.25f),
                                        unfocusedBorderColor = COLOR_SECONDARY.copy(0.15f),
                                        focusedLabelColor = COLOR_SECONDARY.copy(0.5f),
                                        unfocusedLabelColor = COLOR_SECONDARY.copy(0.5f),
                                        placeholderColor = COLOR_SECONDARY.copy(0.25f)
                                    )
                                )
                                Spacer(Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        applicationScope.launch {
                                            isLoading = true
                                            val apiKey = pastedAPI
                                            statusMessage = "⏳ Validation de la clé API par Apollo en cours..."
                                            try {
                                                // <--- Vérifier la validité de la clé ici
                                                statusMessage = "✅ La clé API à bien été validée par Apollo"
                                            }
                                            catch (e: Exception) {statusMessage = "❌ Erreur lors de la validation de la clé API par Apollo : ${e.message}"}
                                            isLoading = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(0.5f),
                                    enabled = pastedAPI.isNotBlank(),
                                    elevation = ButtonDefaults.elevation(10.dp),
                                    shape = RoundedCornerShape(100),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = COLOR_NEUTRAL,
                                        contentColor = COLOR_SECONDARY,
                                        disabledBackgroundColor = COLOR_PRIMARY,
                                        disabledContentColor = COLOR_NEUTRAL,
                                    )
                                ) {
                                    Text("Valider")
                                }
                            }
                            Button(
                                onClick = {
                                    applicationScope.launch {
                                        isLoading = true
                                        statusMessage = "⏳ Exportation du fichier CSV en cours..."
                                        try {
                                            googleSheetsManager.exportToCSV(currentProfile!!, "data_export.csv")
                                            statusMessage = "✅ Exportation du fichier CSV réussie"
                                        }
                                        catch (e: Exception) {statusMessage = "❌ Erreur lors de l'exportation du fichier CSV : ${e.message}"}
                                        isLoading = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = statusMessage == "✅ Extraction des informations réussie" || statusMessage == "⏳ Exportation du fichier CSV en cours...",
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