package ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import utils.FileFormat

@Composable
fun FileImportModal(themeColors: List<Color>, onImportFile: (filePath: String?, format: FileFormat) -> Unit, onDismissRequest: () -> Unit) {
    var importFilePath by remember {mutableStateOf<String?>(null)}
    var importFileFormat by remember {mutableStateOf(FileFormat.CSV)}
    val (darkGray, middleGray, lightGray) = themeColors
    val dialogState = rememberDialogState(WindowPosition.PlatformDefault, DpSize(640.dp, 360.dp))

    DialogWindow(onDismissRequest, dialogState, transparent = true, undecorated = true) {
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
                                Text("Importation", fontSize = 25.sp)
                            }
                            // Bouton de fermeture
                            Row(Modifier, Arrangement.End, Alignment.CenterVertically) {IconButton(onDismissRequest) {Icon(Icons.Filled.Close, "Quitter")}}
                        }
                        SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)
                    }

                    // Contenu
                    Row(Modifier.weight(1f, true).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Zone de texte
                        Column(Modifier.weight(0.525f), Arrangement.Center, Alignment.CenterHorizontally) {
                            Column(Modifier.padding(10.dp, 0.dp).fillMaxWidth(), Arrangement.Center, Alignment.Start) {
                                Text("Fichier à importer :", color = lightGray, fontSize = 20.sp)
                            }
                            Spacer(Modifier.height(5.dp))
                            OutlinedTextField(
                                value = importFilePath ?: "",
                                onValueChange = {importFilePath = it},
                                label = {Text("Sélectionner un fichier...")},
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
                                            importFilePath = "Sélection en cours..."
                                            importFilePath = openDialog("Sélectionner un fichier à importer...")
                                        },
                                        modifier = Modifier.size(25.dp).align(Alignment.CenterHorizontally)
                                    ) {
                                        Icon(Icons.Filled.Search, "Rechercher")
                                    }
                                }
                            )
                        }
                    }

                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)

                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Bouton d'annulation
                        Button(
                            onClick = onDismissRequest,
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

                        // Spacer
                        Spacer(Modifier.weight(0.1f))

                        // Bouton d'importation
                        Button(
                            onClick = {
                                onImportFile(importFilePath, importFileFormat)
                                onDismissRequest()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = importFilePath != null && importFilePath != "Sélection en cours...",
                            elevation = ButtonDefaults.elevation(10.dp),
                            shape = RoundedCornerShape(100),
                            colors = getButtonColors(middleGray, darkGray, lightGray)
                        ) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Check, "")
                                Spacer(Modifier.width(10.dp))
                                Text("Importer")
                            }
                        }
                    }
                }
            }
        }
    }
}