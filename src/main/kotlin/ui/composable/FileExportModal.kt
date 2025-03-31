package ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import utils.FileFormat
import java.awt.FileDialog
import java.awt.Frame

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileExportModal(themeColors: List<Color>, selectedOptions: MutableList<Boolean>, onExport: (String, String, MutableList<Boolean>) -> Unit, onDialogWindowDismissRequest: () -> Unit) {
    var exportFolderPath by remember {mutableStateOf<String?>(null)}
    var exportFileName by remember {mutableStateOf("")}
    var exportFileFormat by remember {mutableStateOf<FileFormat?>(null)}
    val (darkGray, middleGray, lightGray) = themeColors
    val dialogState = rememberDialogState(size = DpSize(640.dp, 500.dp))
    val fullPath = if (exportFolderPath != null && exportFolderPath != "Sélection en cours...") {"$exportFolderPath/$exportFileName"} else {null}

    DialogWindow(onDialogWindowDismissRequest, dialogState, transparent = true, undecorated = true) {
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
                            Row(Modifier, Arrangement.End, Alignment.CenterVertically) {IconButton(onDialogWindowDismissRequest) {Icon(Icons.Filled.Close, "Quitter")}}
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
                                value = exportFolderPath ?: "",
                                onValueChange = {exportFolderPath = it},
                                label = {Text("Sélectionner un dossier...")},
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = lightGray,
                                    focusedBorderColor = lightGray.copy(0.25f),
                                    unfocusedBorderColor = lightGray.copy(0.15f),
                                    focusedLabelColor = lightGray.copy(0.5f),
                                    unfocusedLabelColor = lightGray.copy(0.5f),
                                    placeholderColor = lightGray.copy(0.25f)
                                ),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            exportFolderPath = "Sélection en cours..."
                                            val fileDialog = FileDialog(Frame(), "Sélectionner un dossier d'exportation", FileDialog.LOAD)
                                            fileDialog.file = null
                                            fileDialog.directory = null
                                            System.setProperty("apple.awt.fileDialogForDirectories", "true")
                                            fileDialog.isVisible = true
                                            System.setProperty("apple.awt.fileDialogForDirectories", "false")
                                            exportFolderPath = if (fileDialog.directory != null) {fileDialog.directory} else {null}
                                        },
                                        modifier = Modifier.size(25.dp).align(Alignment.CenterVertically)
                                    ) {
                                        // Icone de loupe
                                        Icon(Icons.Filled.Search, "Rechercher")
                                    }
                                },
//                                visualTransformation = VisualTransformation {text ->
//                                    val trimmedText = if (text.text.length > 40) {"..." + text.text.takeLast(40)} else {text.text}
//                                    TransformedText(AnnotatedString(trimmedText), OffsetMapping.Identity)
//                                }
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            // Texte
                            Text("Nom du fichier :", Modifier.width(200.dp), lightGray, fontSize = 20.sp)
                            // Spacer
                            Spacer(Modifier.height(5.dp))
                            //  Zone du nom d'exportation
                            OutlinedTextField(
                                value = exportFileName,
                                onValueChange = {exportFileName = it},
                                label = {Text("Nom du fichier d'exportation")},
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = lightGray,
                                    focusedBorderColor = lightGray.copy(0.25f),
                                    unfocusedBorderColor = lightGray.copy(0.15f),
                                    focusedLabelColor = lightGray.copy(0.5f),
                                    unfocusedLabelColor = lightGray.copy(0.5f),
                                    placeholderColor = lightGray.copy(0.25f)
                                )
                            )
                        }

                        // Spacer
                        Spacer(Modifier.height(10.dp))

                        // Choix du format
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(Modifier, Arrangement.Center, Alignment.CenterVertically) {
                                // Infobulle
                                TooltipArea({Surface(Modifier.shadow(5.dp), RectangleShape, darkGray) {
                                    Text("Vous pouvez sélectionner plusieurs formats", Modifier.padding(5.dp), color = lightGray)
                                }}) {Icon(Icons.AutoMirrored.Filled.Help, "Aide", Modifier.size(20.dp), lightGray.copy(0.5f))}
                                // Titre
                                Text("Format(s) d'exportation :", Modifier.padding(5.dp), lightGray, fontSize = 20.sp)
                            }
                            // Spacer
                            Spacer(Modifier.height(5.dp))
                            // Bouton segmenté
                            MultiChoiceSegmentedButton(themeColors, selectedOptions, 0.5f)
                        }

                        // Spacer
                        Spacer(Modifier.height(10.dp))

                        Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                            val messageFolderPath = exportFolderPath ?: ""
                            val messageFileName = exportFileName
                            val messageFileFormat = (exportFileFormat ?: "").toString().lowercase()
                            Text("Le fichier sera exporté sous : \"$messageFolderPath\\$messageFileName.$messageFileFormat\"", style = TextStyle(lightGray, textAlign = TextAlign.Center))
                        }
                    }

                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)

                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Bouton d'annulation
                        Button(
                            onClick = onDialogWindowDismissRequest,
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
                        Button(
                            onClick = {onExport(exportFolderPath.toString(), exportFileName, selectedOptions)},
                            modifier = Modifier.weight(1f),
                            enabled = exportFolderPath != null && exportFolderPath != "Sélection en cours..." && exportFileName.isNotBlank() && (selectedOptions[0] || selectedOptions[1]),
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