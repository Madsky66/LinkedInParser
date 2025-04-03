package ui.composable

import manager.FileImportManager
import utils.ConsoleMessage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import config.GlobalConfig
import config.GlobalInstance
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import manager.FileExportManager
import manager.LinkedInManager
import manager.UrlManager
import ui.composable.app.InputSection
import ui.composable.app.ProfileAndOptionsSection
import ui.composable.app.StatusBar
import utils.ConsoleMessageType
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.net.URI

fun processInput(applicationScope: CoroutineScope, input: String, setStatus: (ConsoleMessage) -> Unit, setProfile: (ProspectData?) -> Unit, setLoading: (Boolean) -> Unit) {
    val linkedinManager = LinkedInManager()
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
            setProfile(linkedinManager.extractProfileData(input))
            val newProfile = linkedinManager.extractProfileData(input)
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
fun App(applicationScope: CoroutineScope, gC: GlobalConfig) {
    var consoleMessage by remember {mutableStateOf(ConsoleMessage("En attente de données...", ConsoleMessageType.INFO))}
    var loadedFile by remember {mutableStateOf<File?>(null)}

    val urlManager = remember {UrlManager()}
    val fileImportManager = remember {FileImportManager()}
    val fileExportManager = remember {FileExportManager()}

    val prospectList = remember {mutableStateListOf<ProspectData>()}
    var newProspect by remember {mutableStateOf(ProspectData())}

    var filePath by remember {mutableStateOf("")}
    var fileName by remember {mutableStateOf("")}
    var fileFormat by remember {mutableStateOf("")}

    val statusColor = when (consoleMessage.type) {
        ConsoleMessageType.SUCCESS -> Color.Green.copy(0.9f)
        ConsoleMessageType.ERROR -> Color.Red.copy(0.9f)
        ConsoleMessageType.WARNING -> Color.Yellow.copy(0.9f)
        else -> gC.lightGray.value
    }

    var showExportModal by remember {mutableStateOf(false)}
    var showImportModal by remember {mutableStateOf(false)}

// Modale d'importation
    if (showImportModal) {
        FileImportModal(
            gC= gC,
            onImportFile = {importFilePath ->
                filePath = importFilePath.substringBeforeLast("\\")
                fileName = importFilePath.substringAfterLast("\\").split(".").first()
                fileFormat = importFilePath.split("/").last().split(".").last().lowercase()
                if (importFilePath != "") {
                    applicationScope.launch {
                        gC.consoleMessage.value = ConsoleMessage("⏳ Importation du fichier $fileName.$fileFormat...", ConsoleMessageType.INFO)
                        gC.isImportationLoading.value = true
                        var numberOfColumns = 0
                        try {
                            loadedFile = File(importFilePath)
                            fileImportManager.importFromFile(importFilePath) {importedProspect, filledColumns ->
                                gC.currentProfile.value = importedProspect
                                numberOfColumns = filledColumns
                            }
                            consoleMessage =
                                when (numberOfColumns) {
                                    0 -> ConsoleMessage("❌ Le profil importé est vide", ConsoleMessageType.ERROR)
                                    1,2,3,4,5,6,7 -> ConsoleMessage("⚠️ Le profil importé est incomplet", ConsoleMessageType.WARNING)
                                    else -> ConsoleMessage("✅ Importation du fichier $fileName.$fileFormat réussie", ConsoleMessageType.SUCCESS)
                                }
                        }
                        catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'importation du fichier $fileFormat : ${e.message}", ConsoleMessageType.ERROR)}
                        gC.isImportationLoading.value = false
                    }
                }
                else {gC.consoleMessage.value = ConsoleMessage("⚠️ Aucun fichier sélectionné", ConsoleMessageType.WARNING)}
            },
            onDismissRequest = {showImportModal = false}
        )
    }

    // Modale d'exportation
    if (showExportModal) {
        FileExportModal(
            gC,
            onExport = {exportFolderPath, exportFileName ->
                applicationScope.launch {
                    if (gC.currentProfile.value != null){
                        gC.isExportationLoading.value = true

                        val exportFileFormats = if (gC.selectedOptions[0]) {Pair("XLSX", null)} else if (gC.selectedOptions[1]) {Pair(null, "CSV")} else {Pair("XLSX", "CSV")}
                        val messageFileFormat = if (gC.selectedOptions[0] &&  gC.selectedOptions[1]) {"XLSX, CSV"} else {exportFileFormats.toString()}
                        consoleMessage = ConsoleMessage("⏳ Exportation du fichier au format [$messageFileFormat] en cours...", ConsoleMessageType.INFO)

                        try {
                            val fullExportFolderPathXLSX = "$exportFolderPath\\$exportFileName.xlsx"
                            val fullExportFolderPathCSV = "$exportFolderPath\\$exportFileName.csv"
                            val fullExportFolderPathBoth = exportFolderPath + "\\" + exportFileName + "." + exportFileFormats.toString().lowercase()

                            if (!gC.selectedOptions[0] || !gC.selectedOptions[1]) {fileExportManager.exportToFile(gC, fullExportFolderPathBoth)}
                            else {
                                fileExportManager.exportToFile(gC, fullExportFolderPathXLSX)
                                fileExportManager.exportToFile(gC, fullExportFolderPathCSV)
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
                        gC.isExportationLoading.value = false
                    }
                }
                showExportModal = false
            },
            onDialogWindowDismissRequest = {showExportModal = false}
        )
    }

    Column(Modifier.fillMaxSize().background(gC.middleGray.value).padding(20.dp, 15.dp, 20.dp, 20.dp)) {
        Row(Modifier.weight(0.9f).fillMaxWidth()) {
            // Section du profil et options
            ProfileAndOptionsSection(
                gC, filePath, fileName, fileFormat, {gC.pastedUrl.value = it}, {showImportModal = true}, {showExportModal = true},
                {
                    applicationScope.launch {
                        if (gC.pastedUrl.value.isNotBlank()) {
                            val urlManager = UrlManager()
                            val linkedinManager = LinkedInManager()
                            try {
                                if (!Desktop.isDesktopSupported()) {gC.consoleMessage.value = ConsoleMessage("❌ Votre système ne supporte pas Desktop browsing.", ConsoleMessageType.ERROR); return@launch
                                }
                                val uri = URI("${gC.pastedUrl.value}/overlay/contact-info/")
                                gC.consoleMessage.value = ConsoleMessage("⏳ Ouverture de la page LinkedIn en cours...", ConsoleMessageType.INFO)
                                Desktop.getDesktop().browse(uri)
                                urlManager.openPastedUrl(applicationScope)
                            }
                            catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'ouverture de l'URL : ${e.message}", ConsoleMessageType.ERROR)}
                        }
                    }
                }
            )

            // Spacer
            Spacer(Modifier.width(25.dp))

            // Zone de texte
            InputSection(
                applicationScope, gC,
                onInputChange = {gC.pastedInput.value = it},
                onProcessInput = {input -> processInput(applicationScope, input, setStatus = {consoleMessage = it}, setProfile = {gC.currentProfile.value = it}, setLoading = {gC.isExtractionLoading.value = it})}
            )
        }

        // Spacer
        Spacer(Modifier.height(10.dp))

        // Barre de status
        StatusBar(gC, statusColor)
    }
}