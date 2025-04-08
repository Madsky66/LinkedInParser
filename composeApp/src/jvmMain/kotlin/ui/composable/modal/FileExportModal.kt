package ui.composable.modal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import config.GlobalInstance.config as gC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ui.composable.effect.*
import ui.composable.element.*
import utils.*
import java.awt.*
import javax.swing.JOptionPane

private val showGoogleSheetsPickerModal = mutableStateOf(false)
private val availableSpreadsheets = mutableStateOf<List<Pair<String, String>>>(emptyList())
private val isLoadingSpreadsheets = mutableStateOf(false)

fun onExportModalClose() {
    gC.consoleMessage.value = ConsoleMessage("⚠️ Exportation annulée", ConsoleMessageType.WARNING)
    gC.showExportModal.value = false
    gC.isWaitingForSelection.value = false
    showGoogleSheetsPickerModal.value = false
}

fun onExportConfirm(applicationScope: CoroutineScope) {
    applicationScope.launch {
        try {
            if (!gC.selectedOptions[0] && !gC.selectedOptions[1]) {gC.consoleMessage.value = ConsoleMessage("❌ Aucune option d'exportation sélectionnée", ConsoleMessageType.ERROR); return@launch}
            if (gC.selectedOptions[0]) {
                isLoadingSpreadsheets.value = true
                gC.consoleMessage.value = ConsoleMessage("⏳ Chargement des feuilles Google Sheets...", ConsoleMessageType.INFO)
                try {
                    availableSpreadsheets.value = GoogleSheetsHelper.listAvailableSpreadsheets()
                    if (availableSpreadsheets.value.isEmpty()) {
                        val createNew = JOptionPane.showConfirmDialog(null, "Aucune feuille Google Sheets trouvée. Voulez-vous en créer une nouvelle ?", "Créer une nouvelle feuille", JOptionPane.YES_NO_OPTION)
                        if (createNew == JOptionPane.YES_OPTION) {
                            val sheetName = JOptionPane.showInputDialog(null, "Nom de la nouvelle feuille Google Sheets :", "LinkedInParser - Prospects")
                            if (sheetName != null && sheetName.isNotBlank()) {
                                val newId = GoogleSheetsHelper.createNewSpreadsheet(sheetName)
                                gC.googleSheetsId.value = newId
                                gC.consoleMessage.value = ConsoleMessage("✅ Nouvelle feuille Google Sheets créée", ConsoleMessageType.SUCCESS)
                                // Exporter vers la nouvelle feuille
                                gC.fileExportManager.exportToGoogleSheets()
                            }
                            else {gC.consoleMessage.value = ConsoleMessage("❌ Création de feuille annulée", ConsoleMessageType.ERROR)}
                        }
                    }
                    else {showGoogleSheetsPickerModal.value = true; return@launch}
                }
                catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors du chargement des feuilles : ${e.message}", ConsoleMessageType.ERROR)}
                finally {isLoadingSpreadsheets.value = false}
            }
            // Exporter vers Excel
            if (gC.selectedOptions[1]) {gC.fileExportManager.exportToFile(applicationScope)}
        }
        catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'exportation : ${e.message}", ConsoleMessageType.ERROR)}
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileExportModal(applicationScope: CoroutineScope) {
    val dialogState = rememberDialogState(size = DpSize(640.dp, 500.dp))
    gC.isWaitingForSelection.value = true
    if (gC.selectedOptions.size < 2) {gC.selectedOptions = mutableStateListOf(false, false)}
    val darkGray = gC.darkGray.value
    val middleGray = gC.middleGray.value
    val lightGray = gC.lightGray.value

    // Modale principale d'exportation
    DialogWindow({onExportModalClose()}, dialogState, transparent = true, undecorated = true) {
        WindowDraggableArea(Modifier.fillMaxSize().shadow(5.dp)) {
            Card(Modifier, RectangleShape, backgroundColor = middleGray, contentColor = lightGray, BorderStroke(1.dp, darkGray), elevation = 5.dp) {
                Column(Modifier.padding(20.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                    // Barre de titre
                    Column(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                            // Titre et icone
                            Row(Modifier, Arrangement.Start, Alignment.CenterVertically) {
                                Icon(Icons.Filled.SaveAlt, "", Modifier.size(30.dp))
                                Spacer(Modifier.width(20.dp))
                                Text("Exportation", fontSize = 25.sp)
                            }
                            // Bouton de fermeture
                            Row(Modifier, Arrangement.End, Alignment.CenterVertically) {IconButton({onExportModalClose()}) {Icon(Icons.Filled.Close, "Quitter")}}
                        }
                        SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)
                    }

                    // Contenu
                    Column(Modifier.weight(1f, true).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                        if (gC.selectedOptions[1]) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Row(Modifier, Arrangement.Center, Alignment.CenterVertically) {
                                    // Infobulle
                                    TooltipArea({Surface(Modifier.shadow(5.dp), RectangleShape, darkGray) {
                                        Text("Cliquez sur l'icone en forme de loupe pour sélectionner un chemin d'exportation", Modifier.padding(5.dp), color = lightGray)
                                    }}) {Icon(Icons.AutoMirrored.Filled.Help, "Aide", Modifier.size(20.dp), lightGray.copy(0.5f))}
                                    // Titre
                                    Text("Dossier d'exportation :", Modifier.padding(5.dp), lightGray, fontSize = 20.sp)
                                }
                                // Spacer
                                Spacer(Modifier.height(5.dp))
                                // Zone du chemin d'exportation
                                OutlinedTextField(
                                    value = gC.filePath.value,
                                    onValueChange = {gC.filePath.value = it},
                                    label = {Text("Sélectionner un dossier...")},
                                    singleLine = true,
                                    colors = CustomOutlinedTextFieldColors(),
                                    trailingIcon = {
                                        // Icone de loupe
                                        IconButton(
                                            onClick = {
                                                val fileDialog = FileDialog(Frame(), "Sélectionner un dossier d'exportation", FileDialog.LOAD)
                                                fileDialog.file = null
                                                fileDialog.setFilenameFilter {_, _ -> false}
                                                System.setProperty("apple.awt.fileDialogForDirectories", "true")
                                                fileDialog.isVisible = true
                                                System.setProperty("apple.awt.fileDialogForDirectories", "false")
                                                if (fileDialog.directory != null) {gC.filePath.value = fileDialog.directory.toString()}
                                            },
                                            modifier = Modifier.size(25.dp).align(Alignment.CenterVertically)
                                        ) {
                                            Icon(Icons.Filled.Search, "Rechercher", tint = lightGray)
                                        }
                                    },
                                    visualTransformation = EllipsisVisualTransformation()
                                )
                            }

                            // Spacer
                            Spacer(Modifier.height(10.dp))
                            //
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Row(Modifier, Arrangement.Center, Alignment.CenterVertically) {
                                    // Infobulle
                                    TooltipArea({Surface(Modifier.shadow(5.dp), RectangleShape, darkGray) {
                                        Text("Choisissez un nom de fichier conforme", Modifier.padding(5.dp), color = lightGray)
                                    }}) {Icon(Icons.AutoMirrored.Filled.Help, "Aide", Modifier.size(20.dp), lightGray.copy(0.5f))}
                                    // Titre
                                    Text("Nom du fichier :", Modifier.padding(5.dp), lightGray, fontSize = 20.sp)
                                }
                                // Spacer
                                Spacer(Modifier.height(5.dp))
                                //  Zone du nom d'exportation
                                OutlinedTextField(gC.fileName.value, {gC.fileName.value = it}, label = {Text("Nom du fichier d'exportation")}, singleLine = true, colors = CustomOutlinedTextFieldColors())
                            }
                            //Spacer
                            Spacer(Modifier.height(10.dp))
                        }
                        // Choix du format
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(Modifier, Arrangement.Center, Alignment.CenterVertically) {
                                // Infobulle
                                TooltipArea({Surface(Modifier.shadow(5.dp), RectangleShape, darkGray) {
                                    Text("Vous pouvez sélectionner plusieurs options", Modifier.padding(5.dp), color = lightGray)
                                }}) {Icon(Icons.AutoMirrored.Filled.Help, "Aide", Modifier.size(20.dp), lightGray.copy(0.5f))}
                                // Titre
                                Text("Options d'exportation :", Modifier.padding(5.dp), lightGray, fontSize = 20.sp)
                            }
                            // Options d'exportation
                            Row(Modifier, Arrangement.spacedBy(16.dp), Alignment.CenterVertically) {
                                // Option Google Sheets
                                Row(Modifier, Arrangement.spacedBy(8.dp), Alignment.CenterVertically){
                                    Box(
                                        modifier = Modifier.size(24.dp).clip(CircleShape).background(if (gC.selectedOptions[0]) lightGray else Color.Transparent).border(1.dp, lightGray, CircleShape)
                                            .clickable {gC.selectedOptions[0] = !gC.selectedOptions[0]},
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (gC.selectedOptions[0]) {Icon(Icons.Filled.Check, null, tint = middleGray, modifier = Modifier.size(16.dp))}
                                    }
                                    Text("Google Sheets", color = lightGray)
                                }
                                // Fichier Excel
                                Row(Modifier, Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(24.dp).clip(CircleShape).background(if (gC.selectedOptions[1]) lightGray else Color.Transparent).border(1.dp, lightGray, CircleShape)
                                            .clickable {gC.selectedOptions[1] = !gC.selectedOptions[1]},
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (gC.selectedOptions[1]) {Icon(Icons.Filled.Check, null, tint = middleGray, modifier = Modifier.size(16.dp))}
                                    }
                                    Text("Excel", color = lightGray)
                                }
                            }
                        }
                        // Spacer
                        Spacer(Modifier.height(10.dp))
                        // Texte d'affichage
                        Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                            val selectedExportOption = mutableListOf<String>().apply {
                                if (gC.selectedOptions[0]) add("Google Sheets")
                                if (gC.selectedOptions[1]) add("Excel")
                            }
                            val formattedPath =
                                if (gC.selectedOptions[1]) {
                                    when {
                                        gC.filePath.value.isEmpty() && gC.fileName.value.isEmpty() -> "Aucun emplacement et nom de fichier sélectionnés"
                                        gC.filePath.value.isEmpty() && !gC.fileName.value.isEmpty() -> "Aucun emplacement sélectionné"
                                        !gC.filePath.value.isEmpty() && gC.fileName.value.isEmpty() -> "Aucun nom de fichier sélectionné"
                                        else -> "Le fichier sera exporté sous : ${gC.filePath.value}\\\${gC.fileName.value}\\\${gC.fileFormat.value}"
                                    }
                                }
                                else {"Aucune option d'exportation sélectionnée"}
                            val textColor = if (formattedPath.contains("Aucun")) {Color.Red} else {Color.Green.copy(0.5f)}
                            Text(formattedPath, color = textColor, fontSize = 15.sp, style = TextStyle(lightGray, textAlign = TextAlign.Center))
                        }
                    }

                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)

                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Bouton d'annulation
                        Button({onExportModalClose()}, Modifier.weight(1f), true, elevation = ButtonDefaults.elevation(10.dp), shape = RoundedCornerShape(100), colors = getButtonColors(middleGray, darkGray, lightGray)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Close, "")
                                Spacer(Modifier.width(10.dp))
                                Text("Annuler")
                            }
                        }
                        // Spacer
                        Spacer(Modifier.weight(0.1f))
                        // Bouton d'exportation
                        val isValidFolderPath = gC.filePath.value.matches(Regex("[A-Za-z]:\\\\.*"))
                        val isExcelValid = !gC.selectedOptions[1] || (isValidFolderPath && gC.fileName.value.isNotBlank())
                        val hasSelectedOption = gC.selectedOptions[0] || gC.selectedOptions[1]
                        Button({onExportConfirm(applicationScope)}, Modifier.weight(1f), hasSelectedOption && isExcelValid && !isLoadingSpreadsheets.value, elevation = ButtonDefaults.elevation(10.dp), shape = RoundedCornerShape(100), colors = getButtonColors(middleGray, darkGray, lightGray)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                if (isLoadingSpreadsheets.value) {CircularProgressIndicator(Modifier.size(20.dp), lightGray, 2.dp)} else {Icon(Icons.Filled.Check, "")}
                                Spacer(Modifier.width(10.dp))
                                Text(if (isLoadingSpreadsheets.value) "Chargement..." else "Exporter")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modale de sélection de fichier Google Sheets
    if (showGoogleSheetsPickerModal.value) {
        GoogleSheetsPickerModal(
            spreadsheets = availableSpreadsheets.value,
            onFileSelected = {id ->
                gC.googleSheetsId.value = id
                showGoogleSheetsPickerModal.value = false
                // Exporter vers Google Sheets
                applicationScope.launch {
                    gC.fileExportManager.exportToGoogleSheets()
                    // Exporter vers Excel
                    if (gC.selectedOptions[1]) {gC.fileExportManager.exportToFile(applicationScope)}
                    gC.showExportModal.value = false
                    gC.isWaitingForSelection.value = false
                }
            },
            onCreateNew = {title ->
                applicationScope.launch {
                    try {
                        val newId = GoogleSheetsHelper.createNewSpreadsheet(title)
                        gC.googleSheetsId.value = newId
                        showGoogleSheetsPickerModal.value = false

                        // Exporter vers Google Sheets
                        gC.fileExportManager.exportToGoogleSheets()
                        // Exporter vers Excel
                        if (gC.selectedOptions[1]) {gC.fileExportManager.exportToFile(applicationScope)}

                        gC.showExportModal.value = false
                        gC.isWaitingForSelection.value = false
                    }
                    catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de la création de la feuille : ${e.message}", ConsoleMessageType.ERROR)}
                }
            },
            onDismiss = {showGoogleSheetsPickerModal.value = false}
        )
    }
}