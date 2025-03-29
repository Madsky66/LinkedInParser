package ui.composable

import FileManager
import StatusMessage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import manager.LinkedInManager
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.net.URI

fun processInput(input: String, applicationScope: CoroutineScope, linkedInManager: LinkedInManager, apiKey: String, setStatus: (StatusMessage) -> Unit, setProfile: (ProspectData?) -> Unit, setLoading: (Boolean) -> Unit) {
    applicationScope.launch {
        setLoading(true)
        if (input.isBlank()) {
            setStatus(StatusMessage("En attente de données...", StatusType.INFO))
            setProfile(null)
        }
        else if (input.length < 5000) {
            setStatus(StatusMessage("⚠\uFE0F Trop peu de texte, veuillez vérifier la copie et l'URL de la page (\"http(s)://(www.)linkedin.com/in/...\")", StatusType.WARNING))
            setProfile(null)
        }
        else {
            setLoading(true)
            setStatus(StatusMessage("⏳ Extraction des informations en cours...", StatusType.INFO))
            setProfile(linkedInManager.extractProfileData(input, apiKey))
            val newProfile = linkedInManager.extractProfileData(input, apiKey)
            print(newProfile)
            setStatus(
                if (newProfile.fullName.isBlank() || newProfile.fullName == "Prénom inconnu Nom de famille inconnu") {StatusMessage("❌ Aucune information traitable ou format du texte copié incorrect", StatusType.ERROR)}
                else if (newProfile.firstName == "Prénom inconnu" || newProfile.lastName == "Nom de famille inconnu") {StatusMessage("⚠️ Extraction des données incomplète", StatusType.WARNING)}
                else {StatusMessage("✅ Extraction des informations réussie", StatusType.SUCCESS)}
            )
            setLoading(false)
        }
        setLoading(false)
    }
}

fun openFileDialog(): String? {
    val fileDialog = FileDialog(Frame(), "Importer un fichier", FileDialog.LOAD)
    fileDialog.isVisible = true
    return fileDialog.takeIf {it.file != null}?.let {it.directory + it.file}
}

@Composable
fun getTextFieldColors(colorSecondary: Color) = TextFieldDefaults.outlinedTextFieldColors(
    textColor = colorSecondary,
    focusedBorderColor = colorSecondary.copy(0.25f),
    unfocusedBorderColor = colorSecondary.copy(0.15f),
    focusedLabelColor = colorSecondary.copy(0.5f),
    unfocusedLabelColor = colorSecondary.copy(0.5f),
    placeholderColor = colorSecondary.copy(0.25f)
)

@Composable
fun getButtonColors(backgroundColor: Color, disabledBackgroundColor: Color, contentColor: Color) =
    ButtonDefaults.buttonColors(backgroundColor, contentColor, disabledBackgroundColor, disabledContentColor = contentColor.copy(0.5f))

@Composable
fun SpacedDivider(modifier: Modifier = Modifier, direction: String, thick: Dp = 1.dp, firstSpacer: Dp, secondSpacer: Dp) {
    when (direction) {
        "horizontal" -> Spacer(Modifier.width(firstSpacer))
        "vertical" -> Spacer(Modifier.height(firstSpacer))
    }
    Divider(modifier)
    when (direction) {
        "horizontal" -> Spacer(Modifier.width(firstSpacer))
        "vertical" -> Spacer(Modifier.height(firstSpacer))
    }
}

@Composable
fun App(applicationScope: CoroutineScope, themeColors: List<Color>, apiKey: String?) {
    var pastedInput by remember {mutableStateOf("")}
    var pastedURL by remember {mutableStateOf("")}

    var statusMessage by remember {mutableStateOf(StatusMessage("En attente de données...", StatusType.INFO))}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var showExportModal by remember {mutableStateOf(false)}

    var isExtractionLoading by remember {mutableStateOf(false)}
    var isExportationLoading by remember {mutableStateOf(false)}

    val linkedInManager = remember {LinkedInManager()}
    val fileManager = remember {FileManager()}
    val prospectList = remember {mutableStateListOf<ProspectData>()}
    var newProspect by remember {mutableStateOf(ProspectData())}

    var fileFormat by remember {mutableStateOf(ExportFormat.XLSX)}
    var filePath by remember {mutableStateOf<String?>("")}

    val (darkGray, middleGray, lightGray) = themeColors

    val statusColor = when (statusMessage.type) {
        StatusType.SUCCESS -> Color.Green
        StatusType.ERROR -> Color.Red
        StatusType.WARNING -> Color.Yellow
        else -> lightGray
    }

    Column(Modifier.fillMaxSize().background(middleGray).padding(20.dp, 15.dp, 20.dp, 20.dp)) {
        Row(Modifier.weight(0.9f).fillMaxWidth()) {
            InputSection(
                applicationScope, pastedInput, isExtractionLoading, themeColors,
                onInputChange = {pastedInput = it},
                onProcessInput = {input ->
                    processInput(
                        input, applicationScope, linkedInManager, apiKey.toString(),
                        setStatus = {statusMessage = it},
                        setProfile = {currentProfile = it},
                        setLoading = {isExtractionLoading = it}
                    )
                }
            )

            // Spacer
            Spacer(Modifier.width(15.dp))

            ProfileAndOptionsSection(
                applicationScope, currentProfile, isExtractionLoading, isExportationLoading, pastedURL, statusMessage, themeColors,
                {pastedURL = it},
                {isExtractionLoading = it},
                {isExportationLoading = it},
                {
                    if (pastedURL.isNotBlank()) {
                        try {
                            val uri = URI(pastedURL)
                            if (Desktop.isDesktopSupported()) {Desktop.getDesktop().browse(uri)}
                        }
                        catch (e: Exception) {statusMessage = StatusMessage("❌ Erreur lors de l'ouverture de l'URL : ${e.message}", StatusType.ERROR)}
                    }
                },
                {
                    applicationScope.launch {
                        filePath = openFileDialog()
                        if (filePath != null) {
                            statusMessage = StatusMessage("⏳ Importation du fichier $fileFormat...", StatusType.INFO)
                            try {
                                // importFile(filePath)
                                statusMessage = StatusMessage("✅ Importation du fichier $fileFormat réussie", StatusType.SUCCESS)
                            }
                            catch (e: Exception) {statusMessage = StatusMessage("❌ Erreur lors de l'importation du fichier $fileFormat : ${e.message}", StatusType.ERROR)}
                        }
                        else {statusMessage = StatusMessage("⚠️ Aucun fichier $fileFormat sélectionné", StatusType.WARNING)}
                    }
                },
                {
                    applicationScope.launch {
                        isExportationLoading = true
                        statusMessage = StatusMessage("⏳ Exportation du fichier $fileFormat en cours...", StatusType.INFO)
                        try {
                            when (it) {
                                ExportFormat.XLSX -> {/*fileManager.exportToXLSX(currentProfile!!, filePath)*/}
                                ExportFormat.CSV -> fileManager.exportToCSV(currentProfile!!, filePath.toString())
                            }
                            statusMessage = StatusMessage("✅ Exportation du fichier $fileFormat réussie", StatusType.SUCCESS)
                        }
                        catch (e: Exception) {statusMessage = StatusMessage("❌ Erreur lors de l'exportation du fichier $fileFormat : ${e.message}", StatusType.ERROR)}
                        isExportationLoading = false
                    }
                }
            )
        }
        Spacer(Modifier.height(10.dp))
        StatusBar(statusMessage, statusColor)
    }
}

@Composable
fun RowScope.InputSection(applicationScope: CoroutineScope, pastedInput: String, isLoading: Boolean, themeColors: List<Color>, onInputChange: (String) -> Unit, onProcessInput: (String) -> Unit) {
    val (darkGray, middleGray, lightGray) = themeColors
    Column(Modifier.weight(2.5f).fillMaxHeight(), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = pastedInput,
            onValueChange = {
                applicationScope.launch {
                    onInputChange(it)
                    if (!isLoading) onProcessInput(it)
                }
            },
            label = {Text("Coller le texte de la page LinkedIn ici...")},
            modifier = Modifier.weight(0.9f).fillMaxWidth().clip(RectangleShape),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = lightGray,
                focusedBorderColor = lightGray.copy(0.25f),
                unfocusedBorderColor = lightGray.copy(0.15f),
                focusedLabelColor = lightGray.copy(0.5f),
                unfocusedLabelColor = lightGray.copy(0.5f),
                placeholderColor = lightGray.copy(0.25f)
            )
        )
    }
}

@Composable
fun RowScope.ProfileAndOptionsSection(applicationScope: CoroutineScope, currentProfile: ProspectData?, isExtractionLoading: Boolean, isExportationLoading: Boolean, pastedURL: String, statusMessage: StatusMessage, themeColors: List<Color>, onUrlChange: (String) -> Unit, onExtractionLoading: (Boolean) -> Unit, onExportationLoading: (Boolean) -> Unit, onOpenUrl: (String) -> Unit, onImportFile: () -> Unit, onExportFile: (ExportFormat) -> Unit) {
    var showExportModal by remember {mutableStateOf(false)}
    var (darkGray, middleGray, lightGray) = themeColors

    if (showExportModal) {
        ExportModal(
            onExport = {filepath, selectedFormat ->
//                fileFormat = selectedFormat
                showExportModal = false
                onExportFile(selectedFormat)
            },
            onDismissRequest = {showExportModal = false}
        )
    }

    // Colonne de droite
    Column(Modifier.weight(1f).fillMaxHeight().padding(5.dp, 5.dp, 0.dp, 0.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
        // Fiche contact
        Column(Modifier.fillMaxWidth(), Arrangement.Top, Alignment.CenterHorizontally) {
            if (isExtractionLoading) {CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))}
            else {currentProfile?.let {ProspectCard(it, darkGray, lightGray)} ?: EmptyProspectCard(darkGray, lightGray)}
        }

        // Diviseur espacé
        SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(darkGray.copy(0.05f)), "horizontal", 1.dp, 15.dp, 10.dp)

        // Ouverture du profil LinkedIn
        Column(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterHorizontally) {
            // Saisie de l'URL
            OutlinedTextField(
                value = pastedURL,
                onValueChange = onUrlChange,
                label = {Text("Coller l'URL de la page LinkedIn ici...")},
                modifier = Modifier.fillMaxWidth().clip(RectangleShape),
                colors = getTextFieldColors(lightGray)
            )

            // Bouton de validation
            Button(
                onClick = {onOpenUrl(pastedURL)},
                modifier = Modifier.fillMaxWidth(),
                enabled = pastedURL.matches(Regex("https?://(www\\.)?linkedin\\.com/in/.*")),
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(0, 0, 50, 50),
                colors = getButtonColors(middleGray, darkGray, lightGray)
            ) {
                Text("Ouvrir le profil")
            }
        }

        // Diviseur espacé
        SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(darkGray.copy(0.05f)), "horizontal", 1.dp, 15.dp, 15.dp)

        // Validation de la clé API
        Column(Modifier.fillMaxWidth(), Arrangement.Bottom, Alignment.CenterHorizontally) {


            // Spacer
            Spacer(Modifier.height(10.dp))

            // Bouton d'importation de fichier
            Button(
                onClick = onImportFile,
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(100),
                colors = getButtonColors(middleGray, darkGray, lightGray)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                    Icon(Icons.Filled.SaveAlt, "")
                    Spacer(Modifier.width(10.dp))
                    Text("Importer [CSV / XLSX]")
                }
            }

            Button(
                onClick = {showExportModal = true},
                modifier = Modifier.fillMaxWidth(),
                enabled = currentProfile != null && statusMessage.type == StatusType.SUCCESS,
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(100),
                colors = getButtonColors(middleGray, darkGray, lightGray)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                    Icon(Icons.Filled.IosShare, "")
                    Spacer(Modifier.width(10.dp))
                    Text("Exporter [CSV / XLSX]")
                }
            }
        }
    }
}

@Composable
fun ColumnScope.StatusBar(statusMessage: StatusMessage, statusColor: Color) {
    // Console de statut
    Column(Modifier.weight(0.1f).fillMaxWidth().background(Color.Black), Arrangement.Center) {
        Text(statusMessage.message, Modifier.padding(20.dp, 10.dp), fontSize = 15.sp, color = statusColor)
    }
}