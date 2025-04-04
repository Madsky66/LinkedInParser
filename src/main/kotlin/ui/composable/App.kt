package ui.composable

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
import ui.composable.app.InputSection
import ui.composable.app.ProfileAndOptionsSection
import ui.composable.app.StatusBar
import ui.composable.modal.FileExportModal
import ui.composable.modal.FileImportModal
import utils.ConsoleMessageType
import java.awt.FileDialog
import java.awt.Frame

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