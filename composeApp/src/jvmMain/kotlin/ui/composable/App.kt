package ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import ui.composable.app.LeftSection
import ui.composable.app.ProspectsTable
import ui.composable.app.StatusBar
import ui.composable.modal.FileExportModal
import ui.composable.modal.FileImportModal
import config.GlobalInstance.config as gC

@Composable
fun App(applicationScope: CoroutineScope) {
    val prospectList = remember {mutableStateListOf<ProspectData>()}
    if (gC.showImportModal.value) {FileImportModal(applicationScope) {importedProspects -> prospectList.clear(); prospectList.addAll(importedProspects)}}
    if (gC.showExportModal.value) {FileExportModal(applicationScope)}

    Column(Modifier.fillMaxSize().background(gC.middleGray.value).padding(20.dp, 20.dp, 20.dp, 20.dp)) {
        Row(Modifier.weight(0.95f).fillMaxWidth()) {
            LeftSection(applicationScope)
            Spacer(Modifier.width(25.dp))
            ProspectsTable(prospectList, {prospect -> gC.currentProfile.value = prospect})
        }
        Spacer(Modifier.height(10.dp))
        StatusBar()
    }
}