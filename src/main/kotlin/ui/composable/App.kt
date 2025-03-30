package ui.composable

import FileManager
import androidx.compose.foundation.BorderStroke
import utils.ConsoleMessage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import manager.LinkedInManager
import utils.FileFormat
import utils.ConsoleMessageType
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.net.URI

fun processInput(input: String, applicationScope: CoroutineScope, linkedInManager: LinkedInManager, apiKey: String, setStatus: (ConsoleMessage) -> Unit, setProfile: (ProspectData?) -> Unit, setLoading: (Boolean) -> Unit) {
    applicationScope.launch {
        setLoading(true)
        if (input.isBlank()) {
            setStatus(ConsoleMessage("En attente de données...", ConsoleMessageType.INFO))
            setProfile(null)
        }
        else if (input.length < 5000) {
            setStatus(ConsoleMessage("⚠\uFE0F Trop peu de texte, veuillez vérifier la copie et l'URL de la page (\"http(s)://(www.)linkedin.com/in/...\")", ConsoleMessageType.WARNING))
            setProfile(null)
        }
        else {
            setLoading(true)
            setStatus(ConsoleMessage("⏳ Extraction des informations en cours...", ConsoleMessageType.INFO))
            setProfile(linkedInManager.extractProfileData(input, apiKey))
            val newProfile = linkedInManager.extractProfileData(input, apiKey)
            print(newProfile)
            setStatus(
                if (newProfile.fullName.isBlank() || newProfile.fullName == "Prénom inconnu Nom de famille inconnu") {ConsoleMessage("❌ Aucune information traitable ou format du texte copié incorrect", ConsoleMessageType.ERROR)}
                else if (newProfile.firstName == "Prénom inconnu" || newProfile.lastName == "Nom de famille inconnu") {ConsoleMessage("⚠️ Extraction des données incomplète", ConsoleMessageType.WARNING)}
                else {ConsoleMessage("✅ Extraction des informations réussie", ConsoleMessageType.SUCCESS)}
            )
            setLoading(false)
        }
        setLoading(false)
    }
}

fun openDialog(dialogTitle: String, isVisible: Boolean = true): String? {
    val fileDialog = FileDialog(Frame(), dialogTitle, FileDialog.LOAD)
    fileDialog.isVisible = isVisible
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
fun App(applicationScope: CoroutineScope, themeColors: List<Color>, apiKey: String?) {
    var pastedInput by remember {mutableStateOf("")}
    var pastedURL by remember {mutableStateOf("")}

    var consoleMessage by remember {mutableStateOf(ConsoleMessage("En attente de données...", ConsoleMessageType.INFO))}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var loadedFile by remember {mutableStateOf<File?>(null)}

    var isExtractionLoading by remember {mutableStateOf(false)}
    var isImportationLoading by remember {mutableStateOf(false)}
    var isExportationLoading by remember {mutableStateOf(false)}

    val linkedInManager = remember {LinkedInManager()}
    val fileManager = remember {FileManager()}
    val prospectList = remember {mutableStateListOf<ProspectData>()}
    var newProspect by remember {mutableStateOf(ProspectData())}
    var isIncompleteProspectData by remember {mutableStateOf(false)}

    var fileFormat by remember {mutableStateOf<FileFormat?>(null)}
    var filePath by remember {mutableStateOf<String?>("")}

    val (darkGray, middleGray, lightGray) = themeColors

    val statusColor = when (consoleMessage.type) {
        ConsoleMessageType.SUCCESS -> Color.Green
        ConsoleMessageType.ERROR -> Color.Red
        ConsoleMessageType.WARNING -> Color.Yellow
        else -> lightGray
    }

    var showExportModal by remember {mutableStateOf(false)}
    var showImportModal by remember {mutableStateOf(false)}

    // Modale d'importation
    if (showImportModal) {
        applicationScope.launch {
            val importFilePath = openDialog("Sélectionner un fichier à importer...")
            if (importFilePath != null) {
                consoleMessage = ConsoleMessage("⏳ Importation du fichier $fileFormat...", ConsoleMessageType.INFO)
                try {
                    val importFilePathString = importFilePath.toString()
                    val importFileFullName = importFilePathString.split("/").last()
                    val importFileFormatString = importFileFullName.split(".").last()
                    fileFormat =
                        when (importFileFormatString.lowercase()) {
                            "csv" -> FileFormat.CSV
                            "xlsx" -> FileFormat.XLSX
                            else -> null
                        }
                    if (fileFormat == null) {consoleMessage = ConsoleMessage("❌ Le format du fichier est incorrect [Formats acceptés : XLSX, CSV]", ConsoleMessageType.ERROR)}
                    else {
                        fileManager.importFromFile(importFilePathString, fileFormat) {isIncompleteProspectData = it}
                        consoleMessage =
                            if (isIncompleteProspectData) {ConsoleMessage("⚠️ Le profil importé est incomplet", ConsoleMessageType.WARNING)}
                            else {ConsoleMessage("✅ Importation du fichier $fileFormat réussie", ConsoleMessageType.SUCCESS)}
                    }
                }
                catch (e: Exception) {consoleMessage = ConsoleMessage("❌ Erreur lors de l'importation du fichier $fileFormat : ${e.message}", ConsoleMessageType.ERROR)}
            }
            else {consoleMessage = ConsoleMessage("⚠️ Aucun fichier sélectionné", ConsoleMessageType.WARNING)}
        }
    }

    // Modale d'exportation
    if (showExportModal) {
        FileExportModal(
            themeColors = themeColors,
            onExport = {exportFilePath, exportFileFormat ->
                applicationScope.launch {
                    isExportationLoading = true
                    consoleMessage = ConsoleMessage("⏳ Exportation du fichier $exportFileFormat en cours...", ConsoleMessageType.INFO)
                    try {
                        fileManager.exportToFile(currentProfile!!, exportFilePath.toString(), exportFileFormat)
                        consoleMessage = ConsoleMessage("✅ Exportation du fichier $exportFileFormat réussie", ConsoleMessageType.SUCCESS)
                    }
                    catch (e: Exception) {consoleMessage = ConsoleMessage("❌ Erreur lors de l'exportation du fichier $exportFileFormat : ${e.message}", ConsoleMessageType.ERROR)}
                    isExportationLoading = false
                }
                showExportModal = false
            },
            onDialogWindowDismissRequest = {showExportModal = false}
        )
    }

    Column(Modifier.fillMaxSize().background(middleGray).padding(20.dp, 15.dp, 20.dp, 20.dp)) {
        Row(Modifier.weight(0.9f).fillMaxWidth()) {
            // Zone de texte
            InputSection(
                applicationScope, pastedInput, isExtractionLoading, themeColors,
                onInputChange = {pastedInput = it},
                onProcessInput = {input ->
                    processInput(
                        input, applicationScope, linkedInManager, apiKey.toString(),
                        setStatus = {consoleMessage = it},
                        setProfile = {currentProfile = it},
                        setLoading = {isExtractionLoading = it}
                    )
                }
            )

            // Spacer
            Spacer(Modifier.width(15.dp))

            // Section du profil et  options
            ProfileAndOptionsSection(
                currentProfile, isExtractionLoading, isImportationLoading, isExportationLoading, loadedFile, filePath, fileFormat, pastedURL, consoleMessage, themeColors, {pastedURL = it}, {showImportModal = true}, {showExportModal = true},
                {
                    if (pastedURL.isNotBlank()) {
                        try {
                            val uri = URI(pastedURL)
                            if (Desktop.isDesktopSupported()) {Desktop.getDesktop().browse(uri)}
                        }
                        catch (e: Exception) {consoleMessage = ConsoleMessage("❌ Erreur lors de l'ouverture de l'URL : ${e.message}", ConsoleMessageType.ERROR)}
                    }
                }
            )
        }
        Spacer(Modifier.height(10.dp))
        StatusBar(consoleMessage, statusColor)
    }
}

@Composable
fun RowScope.InputSection(applicationScope: CoroutineScope, pastedInput: String, isLoading: Boolean, themeColors: List<Color>, onInputChange: (String) -> Unit, onProcessInput: (String) -> Unit) {
    val (darkGray, middleGray, lightGray) = themeColors
    Column(Modifier.weight(2.5f).fillMaxHeight().padding(bottom = 5.dp), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
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
fun RowScope.ProfileAndOptionsSection(currentProfile: ProspectData?, isExtractionLoading: Boolean, isImportationLoading: Boolean, isExportationLoading: Boolean, loadedFile: File?, filePath: String?, fileFormat: FileFormat?, pastedURL: String, consoleMessage: ConsoleMessage, themeColors: List<Color>, onUrlChange: (String) -> Unit, onImportButtonClick: () -> Unit, onExportButtonClick: () -> Unit, onOpenUrl: (String) -> Unit) {
    var (darkGray, middleGray, lightGray) = themeColors

    // Colonne de droite
    Column(Modifier.weight(1f).fillMaxHeight().padding(5.dp, 5.dp, 0.dp, 0.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
        // Fiche contact
        Column(Modifier.fillMaxWidth(), Arrangement.Top, Alignment.CenterHorizontally) {currentProfile?.let {ProspectCard(it, themeColors, isImportationLoading, isExtractionLoading)} ?: EmptyProspectCard(themeColors, isImportationLoading, isExtractionLoading)}

        // Diviseur espacé
        SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(darkGray.copy(0.05f)), "horizontal", 1.dp, 15.dp, 10.dp)

        // Profil LinkedIn
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

        // Options
        Column(Modifier.fillMaxWidth(), Arrangement.Bottom, Alignment.CenterHorizontally) {
            var isFileLoaded = (loadedFile != null)
            var loadedFileName = "${loadedFile.toString()}.${fileFormat.toString()}"

            val file = if (isFileLoaded) {loadedFileName} else {"Aucun fichier chargé"}
            val text = "Fichier chargé : "

            // Afficheur de nom de fichier
            Row(Modifier.border(BorderStroke(1.dp, darkGray)).padding(20.dp, 10.dp).fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(text, Modifier, lightGray)
                Text(file, Modifier, color = if (isFileLoaded) {Color.Green.copy(0.5f)} else {lightGray})
            }

            // Spacer
            Spacer(Modifier.height(10.dp))

            // Bouton d'importation de fichier
            Button(
                onClick = onImportButtonClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExtractionLoading && !isImportationLoading && !isExportationLoading,
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
                onClick = onExportButtonClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = currentProfile != null && consoleMessage.type == ConsoleMessageType.SUCCESS,
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
fun ColumnScope.StatusBar(consoleMessage: ConsoleMessage, statusColor: Color) {
    // Console de statut
    Column(Modifier.weight(0.1f).fillMaxWidth().background(Color.Black), Arrangement.Center) {
        Text(consoleMessage.message, Modifier.padding(20.dp, 10.dp), fontSize = 15.sp, color = statusColor)
    }
}