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
import com.google.api.services.drive.Drive
import config.GlobalInstance.config as gC
import kotlinx.coroutines.CoroutineScope
import ui.composable.effect.*
import ui.composable.element.*
import ui.composable.openDialog
import utils.*
import java.awt.*

@Composable
fun openFilePicker () {

}

fun onExportModalClose() {
    gC.consoleMessage.value = ConsoleMessage("⚠️ Exportation annulée", ConsoleMessageType.WARNING)
    gC.showExportModal.value = false
    gC.isWaitingForSelection.value = false
}
fun onExportConfirm(applicationScope: CoroutineScope) {
    if (gC.selectedOptions[0]) {gC.googleSheetsId.value = openFilePicker("").toString()}
    else {gC.fileExportManager.exportToFile(applicationScope)}
    gC.showExportModal.value = false
    gC.isWaitingForSelection.value = false
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileExportModal(applicationScope: CoroutineScope) {
    val dialogState = rememberDialogState(size = DpSize(640.dp, 500.dp))
    gC.isWaitingForSelection.value = true
    if (gC.selectedOptions.size < 3) {gC.selectedOptions = mutableStateListOf(false, false, false)}
    val darkGray = gC.darkGray.value
    val middleGray = gC.middleGray.value
    val lightGray = gC.lightGray.value

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

                        Spacer(Modifier.height(10.dp))

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

                        // Spacer
                        Spacer(Modifier.height(10.dp))

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
                                    Box(Modifier.size(24.dp).clip(CircleShape).background(if (gC.selectedOptions[0]) {lightGray} else Color.Transparent).border(1.dp, lightGray, CircleShape), Alignment.Center) {
                                        Checkbox(gC.selectedOptions[0], {gC.selectedOptions[0] = !gC.selectedOptions[0]}, colors = CheckboxDefaults.colors(Color.Transparent, Color.Transparent, middleGray))
                                    }
                                    Text("Google Sheets", color = lightGray)
                                }
                                // Fichier Excel
                                Row(Modifier, Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                                    Box(Modifier.size(24.dp).clip(CircleShape).background(if (gC.selectedOptions[1]) {lightGray} else Color.Transparent).border(1.dp, lightGray, CircleShape), Alignment.Center) {
                                        Checkbox(gC.selectedOptions[1], {gC.selectedOptions[1] = !gC.selectedOptions[1]}, colors = CheckboxDefaults.colors(Color.Transparent, Color.Transparent, middleGray))
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
                                if (gC.selectedOptions[1]) add("xlsx")
                            }

                            if (gC.selectedOptions[0]) {
                                val formattedPath = when {
                                    gC.filePath.value.isEmpty() && gC.fileName.value.isEmpty() && selectedExportOption.isEmpty() -> "Aucun emplacement, nom ou type de fichier sélectionné"
                                    gC.filePath.value.isEmpty() && gC.fileName.value.isEmpty() -> "Aucun emplacement ou nom de fichier sélectionné"
                                    gC.filePath.value.isEmpty() && selectedExportOption.isEmpty() -> "Aucun emplacement ou type de fichier sélectionné"
                                    gC.fileName.value.isEmpty() && selectedExportOption.isEmpty() -> "Aucun nom ou type de fichier sélectionné"
                                    gC.filePath.value.isEmpty() -> "Aucun emplacement sélectionné"
                                    gC.fileName.value.isEmpty() -> "Aucun nom de fichier sélectionné"
                                    selectedExportOption.isEmpty() -> "Aucun type de fichier sélectionné"
                                    else -> "Le fichier sera exporté sous : ${gC.filePath.value}\\${gC.fileName.value}\\${gC.fileFormat.value}"
                                }
                                val textColor = if (!formattedPath.contains("Aucun")) {Color.Green.copy(0.5f)} else {Color.Red}
                                Text(formattedPath, color = textColor, fontSize = 15.sp, style = TextStyle(lightGray, textAlign = TextAlign.Center))
                            }
                        }
                    }

                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)

                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Bouton d'annulation
                        Button(
                            onClick = {onExportModalClose()},
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            elevation = ButtonDefaults.elevation(10.dp),
                            shape = RoundedCornerShape(100),
                            colors = getButtonColors(middleGray, darkGray, lightGray)
                        ) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Close, "")
                                Spacer(Modifier.width(10.dp))
                                Text("Annuler")
                            }
                        }

                        Spacer(Modifier.weight(0.1f))

                        // Bouton d'exportation
                        val isValidFolderPath = gC.filePath.value.matches(Regex("[A-Za-z]:\\\\.*"))
                        Button(
                            onClick = {onExportConfirm(applicationScope)},
                            modifier = Modifier.weight(1f),
                            enabled = if (gC.selectedOptions[1]) {isValidFolderPath && gC.fileName.value.isNotBlank() /*&& !java.io.File(gC.filePath.value).exists()*/} else {true},
                            elevation = ButtonDefaults.elevation(10.dp),
                            shape = RoundedCornerShape(100),
                            colors = getButtonColors(middleGray, darkGray, lightGray)
                        ) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Check, "")
                                Spacer(Modifier.width(10.dp))
                                Text("Exporter")
                            }
                        }
                    }
                }
            }
        }
    }
}