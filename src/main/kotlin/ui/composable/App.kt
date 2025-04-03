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
import ui.composable.modal.FileExportModal
import ui.composable.modal.FileImportModal
import utils.ConsoleMessageType
import java.awt.FileDialog
import java.awt.Frame

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
    val statusColor = when (gC.consoleMessage.value.type) {
        ConsoleMessageType.SUCCESS -> Color.Green.copy(0.9f)
        ConsoleMessageType.ERROR -> Color.Red.copy(0.9f)
        ConsoleMessageType.WARNING -> Color.Yellow.copy(0.9f)
        else -> gC.lightGray.value
    }

    val prospectList = remember {mutableStateListOf<ProspectData>()}
    var newProspect by remember {mutableStateOf(ProspectData())}

    // Modale d'importation
    if (gC.showImportModal.value) {FileImportModal(applicationScope, gC)}
    // Modale d'exportation
    if (gC.showExportModal.value) {FileExportModal(applicationScope, gC)}

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