package ui.composable.modal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import config.GlobalInstance.config as gC
import data.ProspectData
import kotlinx.coroutines.*
import ui.composable.effect.CustomOutlinedTextFieldColors
import ui.composable.element.*
import utils.*
import java.awt.*

@Composable
fun FileImportModal(applicationScope: CoroutineScope, onProspectsImported: (List<ProspectData>) -> Unit) {
    val dialogState = rememberDialogState(size = DpSize(640.dp, 500.dp))
    val darkGray = gC.darkGray.value
    val middleGray = gC.middleGray.value
    val lightGray = gC.lightGray.value

    var importType by remember {mutableStateOf("")}
    var filePath by remember {mutableStateOf("")}
    var sheetId by remember {mutableStateOf("")}
    var availableSheets by remember {mutableStateOf<List<Pair<String, String>>>(emptyList())}
    var isLoadingSheets by remember {mutableStateOf(false)}
    var showSheetsPickerModal by remember {mutableStateOf(false)}

    DialogWindow({gC.showImportModal.value = false}, dialogState, transparent = true, undecorated = true) {
        WindowDraggableArea(Modifier.fillMaxSize().shadow(5.dp)) {
            Card(Modifier, RectangleShape, backgroundColor = middleGray, contentColor = lightGray, BorderStroke(1.dp, darkGray), elevation = 5.dp) {
                Column(Modifier.padding(20.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                    // Titre
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FileUpload, "", Modifier.size(30.dp))
                            Spacer(Modifier.width(20.dp))
                            Text("Importation", fontSize = 25.sp)
                        }
                        IconButton({gC.showImportModal.value = false}) {Icon(Icons.Filled.Close, "Fermer")}
                    }
                    // Séparateur vertical
                    SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 10.dp, 10.dp)
                    // Contenu
                    Column(Modifier.weight(1f).fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
                        // Options d'importation
                        Text("Sélectionnez une source d'importation :", color = lightGray, fontSize = 18.sp)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                            ImportOptionButton("Fichier Excel", Icons.Filled.InsertDriveFile, importType == "excel", {importType = "excel"})
                            ImportOptionButton("Google Sheets", Icons.Filled.CloudDownload, importType == "sheets", {importType = "sheets"})
                        }
                        Spacer(Modifier.height(16.dp))
                        // Affichage selon le type d'importation
                        when (importType) {
                            "excel" -> {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(filePath, {filePath = it}, Modifier.weight(1f), readOnly = true, label = {Text("Chemin du fichier Excel")}, singleLine = true, colors = CustomOutlinedTextFieldColors(),)
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val fileDialog = FileDialog(Frame(), "Sélectionner un fichier Excel", FileDialog.LOAD)
                                            fileDialog.setFilenameFilter {_, name -> name.endsWith(".xlsx")}
                                            fileDialog.isVisible = true
                                            if (fileDialog.file != null) {filePath = "${fileDialog.directory}${fileDialog.file}"}
                                        }
                                    ) {
                                        Icon(Icons.Filled.FolderOpen, "Parcourir")
                                    }
                                }
                            }
                            "sheets" -> {
                                Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            applicationScope.launch {
                                                isLoadingSheets = true
                                                try {availableSheets = GoogleSheetsHelper.listAvailableSpreadsheets(); showSheetsPickerModal = true}
                                                catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors du chargement des feuilles: ${e.message}", ConsoleMessageType.ERROR)}
                                                finally {isLoadingSheets = false}
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(backgroundColor = middleGray, contentColor = lightGray),
                                        enabled = !isLoadingSheets
                                    ) {
                                        if (isLoadingSheets) {
                                            CircularProgressIndicator(Modifier.size(20.dp), lightGray, 2.dp)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Chargement...")
                                        }
                                        else {
                                            Icon(Icons.Filled.List, "Liste des feuilles")
                                            Spacer(Modifier.width(8.dp))
                                            Text("Afficher les feuilles disponibles")
                                        }
                                    }

                                    if (sheetId.isNotEmpty()) {Text("Feuille sélectionnée: ${availableSheets.find {it.first == sheetId}?.second ?: ""}", color = lightGray)}
                                }
                            }
                        }
                    }
                    //
                    SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 10.dp, 10.dp)
                    // Boutons d'action
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Button(
                            onClick = {gC.showImportModal.value = false},
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(100),
                            colors = getButtonColors(middleGray, darkGray, lightGray)
                        ) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Close, "")
                                Spacer(Modifier.width(10.dp))
                                Text("Annuler")
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Button(
                            onClick = {
                                applicationScope.launch {
                                    gC.isImportationLoading.value = true
                                    try {
                                        val prospects: List<ProspectData> = when (importType) {
                                            "excel" -> {if (filePath.isNotEmpty()) {gC.fileImportManager.importFromFile(applicationScope)} else {emptyList()}}
                                            "sheets" -> {if (sheetId.isNotEmpty()) {gC.fileImportManager.importFromGoogleSheets(sheetId)} else {emptyList()}}
                                            else -> emptyList()
                                        }
                                        if (prospects.isNotEmpty()) {
                                            onProspectsImported(prospects)
                                            gC.showImportModal.value = false
                                        }
                                    }
                                    finally {gC.isImportationLoading.value = false}
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = (importType == "excel" && filePath.isNotEmpty()) || (importType == "sheets" && sheetId.isNotEmpty()),
                            shape = RoundedCornerShape(100),
                            colors = getButtonColors(middleGray, darkGray, lightGray)
                        ) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                if (gC.isImportationLoading.value) {CircularProgressIndicator(Modifier.size(20.dp), lightGray, 2.dp)} else {Icon(Icons.Filled.Check, "")}
                                Spacer(Modifier.width(10.dp))
                                Text(if (gC.isImportationLoading.value) "Importation..." else "Importer")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modale de sélection de feuille Google Sheets
    if (showSheetsPickerModal) {
        GoogleSheetsPickerModal(
            spreadsheets = availableSheets,
            onFileSelected = {id -> sheetId = id; showSheetsPickerModal = false},
            onCreateNew = {title ->
                applicationScope.launch {
                    try {
                        val newId = GoogleSheetsHelper.createNewSpreadsheet(title)
                        sheetId = newId
                        showSheetsPickerModal = false
                    }
                    catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de la création de la feuille: ${e.message}", ConsoleMessageType.ERROR)}
                }
            },
            onDismiss = {showSheetsPickerModal = false}
        )
    }
}

@Composable
private fun ImportOptionButton(text: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val lightGray = gC.lightGray.value
    val middleGray = gC.middleGray.value
    val darkGray = gC.darkGray.value

    Button(
        onClick, Modifier.width(200.dp).height(100.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = if (isSelected) lightGray.copy(0.2f) else middleGray, contentColor = lightGray),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) lightGray else darkGray)
    ) {
        Column(Modifier, Arrangement.Center, Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(text)
        }
    }
}