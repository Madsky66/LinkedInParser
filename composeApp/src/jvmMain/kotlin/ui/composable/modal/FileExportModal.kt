package ui.composable.modal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import config.GlobalInstance.config as gC
import kotlinx.coroutines.CoroutineScope
import ui.composable.effect.*
import ui.composable.element.*
import utils.*
import java.awt.*

fun onExportModalClose() {
    gC.consoleMessage.value = ConsoleMessage("⚠️ Exportation annulée", ConsoleMessageType.WARNING)
    gC.showExportModal.value = false
    gC.isWaitingForSelection.value = false
}
fun onExportConfirm(applicationScope: CoroutineScope) {
    gC.fileExportManager.exportToFile(applicationScope)
    gC.showExportModal.value = false
    gC.isWaitingForSelection.value = false
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileExportModal(applicationScope: CoroutineScope) {
    gC.dialogState.value = DialogState(size = DpSize(640.dp, 500.dp))
    gC.isWaitingForSelection.value = true

    LaunchedEffect(gC.fileFormat.value, gC.selectedOptions) {
        gC.fileFormat.value = when {
            gC.selectedOptions[0] && !gC.selectedOptions[1] -> "xlsx"
            !gC.selectedOptions[0] && gC.selectedOptions[1] -> "csv"
            gC.selectedOptions[0] && gC.selectedOptions[1] -> "xlsx et csv"
            else -> ""
        }
    }

    DialogWindow({onExportModalClose()}, gC.dialogState.value, transparent = true, undecorated = true) {
        WindowDraggableArea(Modifier.fillMaxSize().shadow(5.dp)) {
            Card(Modifier, RectangleShape, backgroundColor = gC.middleGray.value, contentColor = gC.lightGray.value, BorderStroke(1.dp, gC.darkGray.value), elevation = 5.dp) {
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
                        SpacedDivider(Modifier.fillMaxWidth().background(gC.darkGray.value.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)
                    }

                    // Contenu
                    Column(Modifier.weight(1f, true).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(Modifier, Arrangement.Center, Alignment.CenterVertically) {
                                // Infobulle
                                TooltipArea({Surface(Modifier.shadow(5.dp), RectangleShape, gC.darkGray.value) {
                                    Text("Cliquez sur l'icone en forme de loupe pour sélectionner un chemin d'exportation", Modifier.padding(5.dp), color = gC.lightGray.value)
                                }}) {Icon(Icons.AutoMirrored.Filled.Help, "Aide", Modifier.size(20.dp), gC.lightGray.value.copy(0.5f))}
                                // Titre
                                Text("Dossier d'exportation :", Modifier.padding(5.dp), gC.lightGray.value, fontSize = 20.sp)
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
                                        Icon(Icons.Filled.Search, "Rechercher", tint = gC.lightGray.value)
                                    }
                                },
                                visualTransformation = EllipsisVisualTransformation()
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(Modifier, Arrangement.Center, Alignment.CenterVertically) {
                                // Infobulle
                                TooltipArea({Surface(Modifier.shadow(5.dp), RectangleShape, gC.darkGray.value) {
                                    Text("Choisissez un nom de fichier conforme", Modifier.padding(5.dp), color = gC.lightGray.value)
                                }}) {Icon(Icons.AutoMirrored.Filled.Help, "Aide", Modifier.size(20.dp), gC.lightGray.value.copy(0.5f))}
                                // Titre
                                Text("Nom du fichier :", Modifier.padding(5.dp), gC.lightGray.value, fontSize = 20.sp)
                            }
                            // Spacer
                            Spacer(Modifier.height(5.dp))
                            //  Zone du nom d'exportation
                            OutlinedTextField(
                                value = gC.fileName.value,
                                onValueChange = {gC.fileName.value = it},
                                label = {Text("Nom du fichier d'exportation")},
                                singleLine = true,
                                colors = CustomOutlinedTextFieldColors()
                            )
                        }

                        // Spacer
                        Spacer(Modifier.height(10.dp))

                        // Choix du format
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(Modifier, Arrangement.Center, Alignment.CenterVertically) {
                                // Infobulle
                                TooltipArea({Surface(Modifier.shadow(5.dp), RectangleShape, gC.darkGray.value) {
                                    Text("Vous pouvez sélectionner plusieurs formats", Modifier.padding(5.dp), color = gC.lightGray.value)
                                }}) {Icon(Icons.AutoMirrored.Filled.Help, "Aide", Modifier.size(20.dp), gC.lightGray.value.copy(0.5f))}
                                // Titre
                                Text("Format(s) d'exportation :", Modifier.padding(5.dp), gC.lightGray.value, fontSize = 20.sp)
                            }
                            // Spacer
                            Spacer(Modifier.height(5.dp))
                            // Bouton segmenté
                            MultiChoiceSegmentedButton(0.5f)
                        }

                        // Spacer
                        Spacer(Modifier.height(10.dp))

                        // Texte d'affichage
                        Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                            val selectedFormats = mutableListOf<String>().apply {
                                if (gC.selectedOptions[0]) add("xlsx")
                                if (gC.selectedOptions[1]) add("csv")
                            }
                            val textExtension = if (selectedFormats.isNotEmpty()) {selectedFormats.joinToString(" et ") {"${gC.filePath.value}\\${gC.fileName.value}.$it"}} else {""}
                            val formattedPath = when {
                                gC.filePath.value.isEmpty() && gC.fileName.value.isEmpty() && selectedFormats.isEmpty() -> "Aucun emplacement, nom ou type de fichier sélectionné"
                                gC.filePath.value.isEmpty() && gC.fileName.value.isEmpty() -> "Aucun emplacement ou nom de fichier sélectionné"
                                gC.filePath.value.isEmpty() && selectedFormats.isEmpty() -> "Aucun emplacement ou type de fichier sélectionné"
                                gC.fileName.value.isEmpty() && selectedFormats.isEmpty() -> "Aucun nom ou type de fichier sélectionné"
                                gC.filePath.value.isEmpty() -> "Aucun emplacement sélectionné"
                                gC.fileName.value.isEmpty() -> "Aucun nom de fichier sélectionné"
                                selectedFormats.isEmpty() -> "Aucun type de fichier sélectionné"
                                else -> "Le fichier sera exporté sous : $textExtension"
                            }
                            val textColor = if (!formattedPath.contains("Aucun")) {Color.Green.copy(0.5f)} else {Color.Red}
                            Text(formattedPath, color = textColor, fontSize = 15.sp, style = TextStyle(gC.lightGray.value, textAlign = TextAlign.Center))
                        }
                    }

                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(gC.darkGray.value.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)

                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Bouton d'annulation
                        Button(
                            onClick = {onExportModalClose()},
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            elevation = ButtonDefaults.elevation(10.dp),
                            shape = RoundedCornerShape(100),
                            colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
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
                        val hasSelectedFormat = gC.selectedOptions[0] || gC.selectedOptions[1]
                        Button(
                            onClick = {onExportConfirm(applicationScope)},
                            modifier = Modifier.weight(1f),
                            enabled = isValidFolderPath && gC.fileName.value.isNotBlank() && hasSelectedFormat /*&& !java.io.File(gC.filePath.value).exists()*/,
                            elevation = ButtonDefaults.elevation(10.dp),
                            shape = RoundedCornerShape(100),
                            colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
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