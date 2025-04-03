package ui.composable

import utils.ConsoleMessage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import config.GlobalConfig
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import manager.LinkedInManager
import ui.composable.app.InputSection
import ui.composable.app.ProfileAndOptionsSection
import ui.composable.app.StatusBar
import utils.ConsoleMessageType
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.net.URI

fun processInput(applicationScope: CoroutineScope, gC: GlobalConfig, input: String) {
    val linkedinManager = LinkedInManager()
    applicationScope.launch {
        gC.isExtractionLoading.value = true
        if (input.isBlank()) {
            gC.consoleMessage.value = ConsoleMessage("En attente de données...", ConsoleMessageType.INFO)
            gC.currentProfile.value = null
        }
        else if (input.length < 5000) {
            gC.consoleMessage.value = (ConsoleMessage("⚠️ Trop peu de texte, veuillez vérifier la copie et l'URL de la page (\"http(s)://(www.)linkedin.com/in/...\")", ConsoleMessageType.WARNING))
            gC.currentProfile.value = null
        }
        else {
            gC.isExtractionLoading.value = true
            gC.consoleMessage.value = (ConsoleMessage("⏳ Extraction des informations en cours...", ConsoleMessageType.INFO))
            gC.currentProfile.value = linkedinManager.extractProfileData(input)
            val newProfile = linkedinManager.extractProfileData(input)
            gC.consoleMessage.value = (
                if (newProfile.fullName.isBlank() || (newProfile.firstName == "Prénom inconnu" && newProfile.lastName == "Nom de famille inconnu")) {ConsoleMessage("❌ Aucune information traitable ou format du texte copié incorrect", ConsoleMessageType.ERROR)}
                else if (newProfile.firstName == "Prénom inconnu" || newProfile.lastName == "Nom de famille inconnu") {ConsoleMessage("⚠️ Extraction des données incomplète", ConsoleMessageType.WARNING)}
                else {ConsoleMessage("✅ Extraction des informations réussie", ConsoleMessageType.SUCCESS)}
            )
            gC.isExtractionLoading.value = false
        }
        gC.isExtractionLoading.value = false
    }
}

fun openDialog(dialogTitle: String, isVisible: Boolean = true): String? {
    val fileDialog = FileDialog(Frame(), dialogTitle, FileDialog.LOAD)
    fileDialog.isVisible = isVisible
    return if (fileDialog.file != null) {fileDialog.directory + fileDialog.file} else {null}
}

@Composable
fun App(applicationScope: CoroutineScope, gC: GlobalConfig) {
    var loadedFile by remember {mutableStateOf<File?>(null)}

    val prospectList = remember {mutableStateListOf<ProspectData>()}
    var newProspect by remember {mutableStateOf(ProspectData())}

    val statusColor = when (gC.consoleMessage.value.type) {
        ConsoleMessageType.SUCCESS -> Color.Green.copy(0.9f)
        ConsoleMessageType.ERROR -> Color.Red.copy(0.9f)
        ConsoleMessageType.WARNING -> Color.Yellow.copy(0.9f)
        else -> gC.lightGray.value
    }

// Modale d'importation
    if (gC.showImportModal.value) {
        FileImportModal(
            gC= gC,
            onImportFile = {importFilePath ->
                gC.filePath.value = importFilePath.substringBeforeLast("\\")
                gC.fileName.value = importFilePath.substringAfterLast("\\").split(".").first()
                gC.fileFormat.value = importFilePath.split("/").last().split(".").last().lowercase()
                if (importFilePath != "") {
                    applicationScope.launch {
                        gC.consoleMessage.value = ConsoleMessage("⏳ Importation du fichier ${gC.fileName.value}.${gC.fileFormat.value}...", ConsoleMessageType.INFO)
                        gC.isImportationLoading.value = true
                        var numberOfColumns = 0
                        try {
                            loadedFile = File(importFilePath)
                            gC.fileImportManager.importFromFile(importFilePath) {importedProspect, filledColumns ->
                                gC.currentProfile.value = importedProspect
                                numberOfColumns = filledColumns
                            }
                            gC.consoleMessage.value =
                                when (numberOfColumns) {
                                    0 -> ConsoleMessage("❌ Le profil importé est vide", ConsoleMessageType.ERROR)
                                    1,2,3,4,5,6,7 -> ConsoleMessage("⚠️ Le profil importé est incomplet", ConsoleMessageType.WARNING)
                                    else -> ConsoleMessage("✅ Importation du fichier ${gC.fileName.value}.${gC.fileFormat.value} réussie", ConsoleMessageType.SUCCESS)
                                }
                        }
                        catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'importation du fichier ${gC.fileFormat.value} : ${e.message}", ConsoleMessageType.ERROR)}
                        gC.isImportationLoading.value = false
                    }
                }
                else {gC.consoleMessage.value = ConsoleMessage("⚠️ Aucun fichier sélectionné", ConsoleMessageType.WARNING)}
            },
            onDismissRequest = {gC.showImportModal.value = false}
        )
    }

    // Modale d'exportation
    if (gC.showExportModal.value) {
        FileExportModal(
            gC,
            onExport = {exportFolderPath, exportFileName ->
                applicationScope.launch {
                    if (gC.currentProfile.value != null){
                        gC.isExportationLoading.value = true

                        val exportFileFormats = if (gC.selectedOptions[0]) {Pair("XLSX", null)} else if (gC.selectedOptions[1]) {Pair(null, "CSV")} else {Pair("XLSX", "CSV")}
                        val messageFileFormat = if (gC.selectedOptions[0] &&  gC.selectedOptions[1]) {"XLSX, CSV"} else {exportFileFormats.toString()}
                        gC.consoleMessage.value = ConsoleMessage("⏳ Exportation du fichier au format [$messageFileFormat] en cours...", ConsoleMessageType.INFO)

                        try {
                            val fullExportFolderPathXLSX = "$exportFolderPath\\$exportFileName.xlsx"
                            val fullExportFolderPathCSV = "$exportFolderPath\\$exportFileName.csv"
                            val fullExportFolderPathBoth = exportFolderPath + "\\" + exportFileName + "." + exportFileFormats.toString().lowercase()

                            if (!gC.selectedOptions[0] || !gC.selectedOptions[1]) {gC.fileExportManager.exportToFile(gC, fullExportFolderPathBoth)}
                            else {
                                gC.fileExportManager.exportToFile(gC, fullExportFolderPathXLSX)
                                gC.fileExportManager.exportToFile(gC, fullExportFolderPathCSV)
                            }

                            gC.consoleMessage.value = ConsoleMessage("✅ Exportation du fichier $exportFileFormats réussie", ConsoleMessageType.SUCCESS)
                            try {
                                val sheetsUrl = "https://docs.google.com/spreadsheets/u/0/create"
                                val uri = URI(sheetsUrl)
                                if (Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().browse(uri)
                                    gC.consoleMessage.value = ConsoleMessage("✅ Google Sheets ouvert. Vous pouvez maintenant importer votre fichier.", ConsoleMessageType.SUCCESS)
                                }
                            }
                            catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("⚠️ Exportation réussie mais impossible d'ouvrir Google Sheets : ${e.message}", ConsoleMessageType.WARNING)}
                        }
                        catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'exportation du fichier $exportFileFormats : ${e.message}", ConsoleMessageType.ERROR)}
                        gC.isExportationLoading.value = false
                    }
                }
                gC.showExportModal.value = false
            },
            onDialogWindowDismissRequest = {gC.showExportModal.value = false}
        )
    }

    Column(Modifier.fillMaxSize().background(gC.middleGray.value).padding(20.dp, 15.dp, 20.dp, 20.dp)) {
        Row(Modifier.weight(0.9f).fillMaxWidth()) {
            // Section du profil et options
            ProfileAndOptionsSection(applicationScope, gC)
            // Spacer
            Spacer(Modifier.width(25.dp))
            // Zone de texte
            InputSection(applicationScope, gC)
        }
        // Spacer
        Spacer(Modifier.height(10.dp))
        // Barre de status
        StatusBar(gC, statusColor)
    }
}