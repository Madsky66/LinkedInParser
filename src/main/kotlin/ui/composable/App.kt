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
import androidx.compose.ui.window.DialogWindow
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import manager.LinkedInManager
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.net.URI

enum class StatusType {INFO, SUCCESS, ERROR, WARNING}
data class StatusMessage(val message: String, val type: StatusType)

fun openFileDialog(): String? {
    val fileDialog = FileDialog(Frame(), "Importer un fichier", FileDialog.LOAD)
    fileDialog.isVisible = true
    return if (fileDialog.file != null) {fileDialog.directory + fileDialog.file} else {null}
}

@Composable
fun App(applicationScope: CoroutineScope, COLOR_PRIMARY: Color, COLOR_NEUTRAL: Color, COLOR_SECONDARY: Color) {
    var pastedInput by remember {mutableStateOf("")}
    var pastedURL by remember {mutableStateOf("")}
    var pastedAPI by remember {mutableStateOf("")}

    var apiKey by remember {mutableStateOf("")}

    var statusMessage by remember {mutableStateOf(StatusMessage("En attente de données...", StatusType.INFO))}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var showExportModal by remember {mutableStateOf(false)}
    var isLoading by remember {mutableStateOf(false)}

    val linkedInManager = LinkedInManager()
    val googleSheetsManager = remember {GoogleSheetsManager()}
    val prospectList = remember {mutableStateListOf<ProspectData>()}
    var newProspect by remember {mutableStateOf(ProspectData())}

    val statusColor = when (statusMessage.type) {
        StatusType.SUCCESS -> Color.Green
        StatusType.ERROR -> Color.Red
        StatusType.WARNING -> Color.Yellow
        else -> COLOR_SECONDARY
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(COLOR_NEUTRAL).padding(20.dp, 15.dp, 20.dp, 20.dp)) {
            Row(Modifier.weight(0.9f).fillMaxWidth()) {
                // Zone de texte
                Column(Modifier.weight(2f).fillMaxHeight(), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = pastedInput,
                        onValueChange = {
                            pastedInput = it
                            if (pastedInput == "") {applicationScope.launch {statusMessage = StatusMessage("En attente de données...", StatusType.INFO)}}
                            else {
                                applicationScope.launch {
                                    isLoading = true
                                    statusMessage = StatusMessage("⏳ Extraction des informations en cours...", StatusType.INFO)
                                    currentProfile = linkedInManager.extractProfileData(pastedInput, apiKey)
                                    statusMessage =
                                        if (currentProfile?.fullName == "") {StatusMessage("❌ Aucune information extraite", StatusType.ERROR)}
                                        else if (currentProfile?.fullName != "Prénom iconnu Nom de famille inconnu") {statusMessage = StatusMessage("❌ Erreur lors de l'extraction des informations", StatusType.ERROR)}
                                        else if (currentProfile?.firstName == "Prénom inconnu" || currentProfile?.lastName == "Nom de famille inconnu") {StatusMessage("⚠\uFE0F Extraction des données incomplète", StatusType.WARNING)}
                                        else {StatusMessage("✅ Extraction des informations réussie", StatusType.SUCCESS)}
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
                                        catch (e: Exception) {statusMessage = StatusMessage("❌ Erreur lors de l'ouverture de l'URL : ${e.message}", StatusType.ERROR)}
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
                                            apiKey = pastedAPI
                                            statusMessage = StatusMessage("⏳ Validation de la clé API par Apollo en cours...", StatusType.INFO)
                                            try {
                                                // <--- Vérifier la validité de la clé ici
                                                statusMessage = StatusMessage("✅ La clé API à bien été validée par Apollo", StatusType.SUCCESS)
                                            }
                                            catch (e: Exception) {statusMessage = StatusMessage("❌ Erreur lors de la validation de la clé API par Apollo : ${e.message}", StatusType.ERROR)}
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
                                        val filePath = openFileDialog()
                                        if (filePath != null) {
                                            statusMessage = StatusMessage("⏳ Importation du fichier...", StatusType.INFO)
                                            try {
                                                // importFile(filePath)
                                                statusMessage = StatusMessage("✅ Importation réussie", StatusType.SUCCESS)
                                            }
                                            catch (e: Exception) {statusMessage = StatusMessage("❌ Erreur lors de l'importation : ${e.message}", StatusType.ERROR)}
                                        }
                                        else {statusMessage = StatusMessage("⚠️ Aucune sélection de fichier", StatusType.WARNING)}
                                    }
                                },
                            ) {
                                Text("Importer")
                            }

                            Button(
                                onClick = {showExportModal = true},
                                modifier = Modifier.fillMaxWidth(),
                                enabled = statusMessage == StatusMessage("✅ Extraction des informations réussie", StatusType.SUCCESS),
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
                            if (showExportModal) {
                                ExportModal(
                                    onExport = {filePath, format ->
                                        showExportModal = false
                                        applicationScope.launch {
                                            isLoading = true
                                            statusMessage = StatusMessage("⏳ Exportation du fichier CSV en cours...", StatusType.INFO)
                                            try {
                                                when (format) {
                                                    ExportFormat.CSV -> googleSheetsManager.exportToCSV(currentProfile!!, filePath)
                                                    ExportFormat.XLSX -> {/*googleSheetsManager.exportToXLSX(currentProfile!!, filePath)*/}
                                                }
                                                statusMessage = StatusMessage("✅ Exportation du fichier CSV réussie", StatusType.SUCCESS)
                                            }
                                            catch (e: Exception) {statusMessage = StatusMessage("❌ Erreur lors de l'exportation du fichier CSV : ${e.message}", StatusType.ERROR)}
                                            isLoading = false
                                        }
                                    },
                                    onDismissRequest = {showExportModal = false}
                                )
                            }
                        }
                    }
                }
            }

            // Spacer
            Spacer(Modifier.height(10.dp))

            // Console
            Column(Modifier.weight(0.1f).fillMaxWidth().background(Color.Black), Arrangement.Center) {
                Text(statusMessage.message, Modifier.padding(20.dp, 10.dp), fontSize = 15.sp, color = statusColor)
            }
        }
    }
}

@Composable
fun ExportModal(onExport: (filePath: String, format: ExportFormat) -> Unit, onDismissRequest: () -> Unit) {
    var filePath by remember {mutableStateOf("")}
    var selectedFormat by remember {mutableStateOf(ExportFormat.CSV)}

    DialogWindow(onCloseRequest = onDismissRequest) {
        Surface(shape = RoundedCornerShape(8.dp), color = Color.White) {
            Column(Modifier.padding(16.dp)) {
                Text("Exporter", fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = filePath,
                    onValueChange = {filePath = it},
                    label = {Text("Chemin du fichier")},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Format:")
                Row(Modifier.fillMaxWidth()) {
                    RadioButton(selected = (selectedFormat == ExportFormat.CSV), onClick = {selectedFormat = ExportFormat.CSV})
                    Text("CSV", Modifier.align(Alignment.CenterVertically))
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = (selectedFormat == ExportFormat.XLSX), onClick = {selectedFormat = ExportFormat.XLSX})
                    Text("XLSX", modifier = Modifier.align(Alignment.CenterVertically))
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismissRequest) {Text("Annuler")}
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {onExport(filePath, selectedFormat)}) {Text("Exporter")}
                }
            }
        }
    }
}

enum class ExportFormat {CSV, XLSX}