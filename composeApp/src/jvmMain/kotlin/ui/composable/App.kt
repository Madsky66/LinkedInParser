package ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import ui.composable.app.InputSection
import ui.composable.app.ProfileAndOptionsSection
import ui.composable.app.StatusBar
import ui.composable.modal.FileExportModal
import ui.composable.modal.FileImportModal
import java.awt.FileDialog
import java.awt.Frame
import config.GlobalInstance.config as gC

fun openDialog(dialogTitle: String, isVisible: Boolean = true): String? {
    val fileDialog = FileDialog(Frame(), dialogTitle, FileDialog.LOAD)
    fileDialog.isVisible = isVisible
    return if (fileDialog.file != null) {fileDialog.directory + fileDialog.file} else {null}
}

@Composable
fun App(applicationScope: CoroutineScope) {
    val prospectList = remember {mutableStateListOf<ProspectData>()}
    var newProspect by remember {mutableStateOf(ProspectData())}

    // Modale d'importation
    if (gC.showImportModal.value) {FileImportModal(applicationScope)}
    // Modale d'exportation
    if (gC.showExportModal.value) {FileExportModal(applicationScope)}

    Column(Modifier.fillMaxSize().background(gC.middleGray.value).padding(20.dp, 15.dp, 20.dp, 20.dp)) {
        Row(Modifier.weight(0.9f).fillMaxWidth()) {
            // Section du profil et options
            ProfileAndOptionsSection(applicationScope)
            // Spacer
            Spacer(Modifier.width(25.dp))
            // Zone de texte
            InputSection(applicationScope)
        }
        // Spacer
        Spacer(Modifier.height(10.dp))
        // Barre de status
        StatusBar()
    }
}