package ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import config.GlobalConfig
import utils.getButtonColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileImportModal(gC: GlobalConfig, onImportFile: (importFilePath: String) -> Unit, onDismissRequest: () -> Unit) {
    val dialogState = rememberDialogState(WindowPosition.PlatformDefault, DpSize(640.dp, 360.dp))

    var importFilePath by remember {mutableStateOf("")}
    var importFileName by remember {mutableStateOf("")}
    var importFileFormat by remember {mutableStateOf("")}

    val isPathCorrect = (importFilePath.matches(Regex("[A-Za-z]:\\\\.*")) == true) && (importFileFormat.lowercase() == "xlsx" || importFileFormat.lowercase() == "csv")
    val formatColor = when (importFileFormat) {
        "" -> gC.lightGray.value
        "csv", "xlsx" -> Color.Green.copy(0.5f)
        else -> Color.Red.copy(0.5f)
    }

    LaunchedEffect(importFilePath) {importFileFormat = if (importFilePath != "") {importFilePath.substringAfterLast('.', "").lowercase()} else {""}}

    DialogWindow(onDismissRequest, dialogState, transparent = true, undecorated = true) {
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
                                Text("Importation", fontSize = 25.sp)
                            }
                            // Bouton de fermeture
                            Row(Modifier, Arrangement.End, Alignment.CenterVertically) {IconButton(onDismissRequest) {Icon(Icons.Filled.Close, "Quitter")}}
                        }
                        SpacedDivider(Modifier.fillMaxWidth().background(gC.darkGray.value.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)
                    }

                    // Contenu
                    Row(Modifier.weight(1f, true).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.fillMaxSize().padding(20.dp), Arrangement.Center, Alignment.CenterHorizontally) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Row(Modifier, Arrangement.Center, Alignment.CenterVertically) {
                                    // Infobulle
                                    TooltipArea({Surface(Modifier.shadow(5.dp), RectangleShape, gC.darkGray.value) {
                                        Text("Cliquez sur l'icone en forme de loupe pour sélectionner un fichier à importer", Modifier.padding(5.dp), color = gC.lightGray.value)
                                    }}) {Icon(Icons.AutoMirrored.Filled.Help, "Aide", Modifier.size(20.dp), gC.lightGray.value.copy(0.5f))}
                                    // Titre
                                    Text("Fichier à importer :", Modifier.padding(5.dp), gC.lightGray.value, fontSize = 20.sp)
                                }
                                // Afficheur de format
                                Box(Modifier.widthIn(min = 50.dp).background(gC.darkGray.value, RoundedCornerShape(50)).padding(10.dp, 5.dp), Alignment.Center) {Text(importFileFormat, color = formatColor, fontSize = 17.sp)}
                            }

                            // Spacer
                            Spacer(Modifier.height(5.dp))

                            // Zone du chemin d 'importation
                            OutlinedTextField(
                                value = importFilePath,
                                onValueChange = {importFilePath = it},
                                modifier = Modifier.fillMaxWidth(),
                                label = {Text("Sélectionner un fichier...")},
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = gC.lightGray.value.copy(0.5f),
                                    focusedBorderColor = gC.lightGray.value.copy(0.25f),
                                    unfocusedBorderColor = gC.lightGray.value.copy(0.15f),
                                    focusedLabelColor = gC.lightGray.value.copy(0.5f),
                                    unfocusedLabelColor = gC.lightGray.value.copy(0.5f)
                                ),
                                trailingIcon = {
                                    // Icone de loupe
                                    IconButton({importFilePath = openDialog("Sélectionner un fichier à importer...").toString()}, Modifier.size(25.dp).align(Alignment.CenterHorizontally)) {
                                        Icon(Icons.Filled.Search, "Rechercher", tint = gC.lightGray.value
                                        )
                                    }
                                },
                                visualTransformation = VisualTransformation {text ->
                                    if (text.text.length > 30) {
                                        val displayText = "..." + text.text.takeLast(27)
                                        TransformedText(AnnotatedString(displayText), object : OffsetMapping {
                                            override fun originalToTransformed(offset: Int): Int {return if (offset <= text.text.length - 27) 3 else offset - (text.text.length - 27) + 3}
                                            override fun transformedToOriginal(offset: Int): Int {return if (offset <= 3) 0 else offset - 3 + (text.text.length - 27)}
                                        })
                                    }
                                    else {TransformedText(AnnotatedString(text.text), OffsetMapping.Identity)}
                                }
                            )
                        }
                    }

                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(gC.darkGray.value.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)

                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Bouton d'annulation
                        Button(onDismissRequest, Modifier.weight(1f), enabled = true, elevation = ButtonDefaults.elevation(10.dp), shape = RoundedCornerShape(100), colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)) {
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
                                onImportFile(importFilePath)
                                onDismissRequest()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = isPathCorrect,
                            elevation = ButtonDefaults.elevation(10.dp),
                            shape = RoundedCornerShape(100),
                            colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
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