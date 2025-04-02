package ui.composable

import manager.FileManager
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import manager.LinkedInManager
import ui.composable.app.InputSection
import ui.composable.app.ProfileAndOptionsSection
import ui.composable.app.StatusBar
import utils.ConsoleMessageType
import utils.copyUrlContent
import utils.modalDetectionStep
import utils.getClipboardContent
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Robot
import java.awt.Toolkit
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
            setStatus(ConsoleMessage("⚠️ Trop peu de texte, veuillez vérifier la copie et l'URL de la page (\"http(s)://(www.)linkedin.com/in/...\")", ConsoleMessageType.WARNING))
            setProfile(null)
        }
        else {
            setLoading(true)
            setStatus(ConsoleMessage("⏳ Extraction des informations en cours...", ConsoleMessageType.INFO))
            setProfile(linkedInManager.extractProfileData(input, apiKey))
            val newProfile = linkedInManager.extractProfileData(input, apiKey)
            setStatus(
                if (newProfile.fullName.isBlank() || (newProfile.firstName == "Prénom inconnu" && newProfile.lastName == "Nom de famille inconnu")) {ConsoleMessage("❌ Aucune information traitable ou format du texte copié incorrect", ConsoleMessageType.ERROR)}
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
    return if (fileDialog.file != null) {fileDialog.directory + fileDialog.file} else {null}
}

@Composable
fun App(applicationScope: CoroutineScope, themeColors: List<Color>, apiKey: String) {
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

    var filePath by remember {mutableStateOf("")}
    var fileName by remember {mutableStateOf("")}
    var fileFormat by remember {mutableStateOf("")}

    val (darkGray, middleGray, lightGray) = themeColors

    val statusColor = when (consoleMessage.type) {
        ConsoleMessageType.SUCCESS -> Color.Green.copy(0.9f)
        ConsoleMessageType.ERROR -> Color.Red.copy(0.9f)
        ConsoleMessageType.WARNING -> Color.Yellow.copy(0.9f)
        else -> lightGray
    }

    var showExportModal by remember {mutableStateOf(false)}
    var showImportModal by remember {mutableStateOf(false)}

// Modale d'importation
    if (showImportModal) {
        FileImportModal(
            themeColors = themeColors,
            onImportFile = {importFilePath ->
                filePath = importFilePath.substringBeforeLast("\\")
                fileName = importFilePath.substringAfterLast("\\").split(".").first()
                fileFormat = importFilePath.split("/").last().split(".").last().lowercase()
                if (importFilePath != "") {
                    applicationScope.launch {
                        consoleMessage = ConsoleMessage("⏳ Importation du fichier $fileName.$fileFormat...", ConsoleMessageType.INFO)
                        isImportationLoading = true
                        var numberOfColumns = 0
                        try {
                            loadedFile = File(importFilePath)
                            fileManager.importFromFile(importFilePath) {importedProspect, filledColumns ->
                                currentProfile = importedProspect
                                numberOfColumns = filledColumns
                            }
                            consoleMessage =
                                when (numberOfColumns) {
                                    0 -> ConsoleMessage("❌ Le profil importé est vide", ConsoleMessageType.ERROR)
                                    1,2,3,4,5,6,7 -> ConsoleMessage("⚠️ Le profil importé est incomplet", ConsoleMessageType.WARNING)
                                    else -> ConsoleMessage("✅ Importation du fichier $fileName.$fileFormat réussie", ConsoleMessageType.SUCCESS)
                                }
                        }
                        catch (e: Exception) {consoleMessage = ConsoleMessage("❌ Erreur lors de l'importation du fichier $fileFormat : ${e.message}", ConsoleMessageType.ERROR)}
                        isImportationLoading = false
                    }
                }
                else {consoleMessage = ConsoleMessage("⚠️ Aucun fichier sélectionné", ConsoleMessageType.WARNING)}
            },
            onDismissRequest = {showImportModal = false}
        )
    }

    // Modale d'exportation
    if (showExportModal) {
        val selectedOptions = rememberSaveable {mutableStateListOf(false, false)}
        FileExportModal(
            themeColors, selectedOptions,
            onExport = {exportFolderPath, exportFileName, selectedOptions ->
                applicationScope.launch {
                    if (currentProfile != null){
                        isExportationLoading = true

                        val exportFileFormats = if (selectedOptions[0]) {Pair("XLSX", null)} else if (selectedOptions[1]) {Pair(null, "CSV")} else {Pair("XLSX", "CSV")}
                        val messageFileFormat = if (selectedOptions[0] &&  selectedOptions[1]) {"XLSX, CSV"} else {exportFileFormats.toString()}
                        consoleMessage = ConsoleMessage("⏳ Exportation du fichier au format [$messageFileFormat] en cours...", ConsoleMessageType.INFO)

                        try {
                            val fullExportFolderPathXLSX = "$exportFolderPath\\$exportFileName.xlsx"
                            val fullExportFolderPathCSV = "$exportFolderPath\\$exportFileName.csv"
                            val fullExportFolderPathBoth = exportFolderPath + "\\" + exportFileName + "." + exportFileFormats.toString().lowercase()

                            if (!selectedOptions[0] || !selectedOptions[1]) {fileManager.exportToFile(currentProfile!!, fullExportFolderPathBoth)}
                            else {
                                fileManager.exportToFile(currentProfile!!, fullExportFolderPathXLSX)
                                fileManager.exportToFile(currentProfile!!, fullExportFolderPathCSV)
                            }

                            consoleMessage = ConsoleMessage("✅ Exportation du fichier $exportFileFormats réussie", ConsoleMessageType.SUCCESS)
                            try {
                                val sheetsUrl = "https://docs.google.com/spreadsheets/u/0/create"
                                val uri = URI(sheetsUrl)
                                if (Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().browse(uri)
                                    consoleMessage = ConsoleMessage("✅ Google Sheets ouvert. Vous pouvez maintenant importer votre fichier.", ConsoleMessageType.SUCCESS)
                                }
                            }
                            catch (e: Exception) {consoleMessage = ConsoleMessage("⚠️ Exportation réussie mais impossible d'ouvrir Google Sheets : ${e.message}", ConsoleMessageType.WARNING)}
                        }
                        catch (e: Exception) {consoleMessage = ConsoleMessage("❌ Erreur lors de l'exportation du fichier $exportFileFormats : ${e.message}", ConsoleMessageType.ERROR)}
                        isExportationLoading = false
                    }
                }
                showExportModal = false
            },
            onDialogWindowDismissRequest = {showExportModal = false}
        )
    }

    Column(Modifier.fillMaxSize().background(middleGray).padding(20.dp, 15.dp, 20.dp, 20.dp)) {
        Row(Modifier.weight(0.9f).fillMaxWidth()) {
            // Section du profil et options
            ProfileAndOptionsSection(
                currentProfile, isExtractionLoading, isImportationLoading, isExportationLoading, filePath, fileName, fileFormat, pastedURL, consoleMessage, themeColors, {pastedURL = it}, {showImportModal = true}, {showExportModal = true},
                {
                    if (pastedURL.isNotBlank()) {
                        try {
                            if (!Desktop.isDesktopSupported()) {consoleMessage = ConsoleMessage("❌ Votre système ne supporte pas Desktop browsing.", ConsoleMessageType.ERROR); return@ProfileAndOptionsSection}
                            val uri = URI("$pastedURL/overlay/contact-info/")
                            consoleMessage = ConsoleMessage("⏳ Ouverture de la page LinkedIn en cours...", ConsoleMessageType.INFO)
                            Desktop.getDesktop().browse(uri)
                            applicationScope.launch {
                                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                val robot = Robot()
                                delay(5000) // <--- Rendre cette valeur dynamique

                                consoleMessage = ConsoleMessage("⏳ Détection de la page de profil en cours...", ConsoleMessageType.INFO)

                                copyUrlContent(robot)
                                var clipboardContent = getClipboardContent(clipboard)

                                val isModalOpen = clipboardContent.lines().take(5).any {it.contains("dialogue")}
                                if (!isModalOpen && !modalDetectionStep({clipboardContent.lines().take(5).any {it.contains("dialogue")}}, robot, clipboard, "Détection de la page de profil impossible", {clipboardContent = it}, {consoleMessage = it})) return@launch
                                if (isModalOpen && clipboardContent.length <= 5000 && !modalDetectionStep({clipboardContent.length > 5000}, robot, clipboard, "Quantité de données insuffisante", {clipboardContent = it}, {consoleMessage = it})) return@launch

                                consoleMessage = ConsoleMessage("⏳ Analyse des données en cours...", ConsoleMessageType.INFO)
                                pastedInput = clipboardContent
                                processInput(clipboardContent, applicationScope, linkedInManager, apiKey.toString(), setStatus = {consoleMessage = it}, setProfile = {currentProfile = it}, setLoading = {isExtractionLoading = it})
                            }
                        }
                        catch (e: Exception) {consoleMessage = ConsoleMessage("❌ Erreur lors de l'ouverture de l'URL : ${e.message}", ConsoleMessageType.ERROR)}
                    }
                }
            )

            // Spacer
            Spacer(Modifier.width(25.dp))

            // Zone de texte
            InputSection(
                applicationScope, pastedInput, isExtractionLoading, themeColors,
                onInputChange = {pastedInput = it},
                onProcessInput = {input -> processInput(input, applicationScope, linkedInManager, apiKey.toString(), setStatus = {consoleMessage = it}, setProfile = {currentProfile = it}, setLoading = {isExtractionLoading = it})}
            )
        }

        // Spacer
        Spacer(Modifier.height(10.dp))

        // Barre de status
        StatusBar(consoleMessage, statusColor)
    }
}