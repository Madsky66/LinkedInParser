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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import utils.getButtonColors
import java.awt.FileDialog
import java.awt.Frame

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileExportModal(themeColors: List<Color>, selectedOptions: MutableList<Boolean>, onExport: (String, String, MutableList<Boolean>) -> Unit, onDialogWindowDismissRequest: () -> Unit) {
    val (darkGray, middleGray, lightGray) = themeColors
    val dialogState = rememberDialogState(size = DpSize(640.dp, 500.dp))

    var exportFolderPath by remember {mutableStateOf("")}
    var exportFileName by remember {mutableStateOf("")}
    var exportFileFormat by remember {mutableStateOf("")}

    LaunchedEffect(exportFolderPath, exportFileName, selectedOptions) {
        exportFileFormat = when {
            selectedOptions[0] && !selectedOptions[1] -> "xlsx"
            !selectedOptions[0] && selectedOptions[1] -> "csv"
            selectedOptions[0] && selectedOptions[1] -> "xlsx et csv"
            else -> ""
        }
    }

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
                                value = exportFolderPath,
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
                                    // Icone de loupe
                                    IconButton(
                                        onClick = {
                                            val fileDialog = FileDialog(Frame(), "Sélectionner un dossier d'exportation", FileDialog.LOAD)
                                            fileDialog.file = null
                                            fileDialog.setFilenameFilter {_, _ -> false}
                                            System.setProperty("apple.awt.fileDialogForDirectories", "true")
                                            fileDialog.isVisible = true
                                            System.setProperty("apple.awt.fileDialogForDirectories", "false")
                                            if (fileDialog.directory != null) {exportFolderPath = fileDialog.directory.toString()}
                                        },
                                        modifier = Modifier.size(25.dp).align(Alignment.CenterVertically)
                                    ) {
                                        Icon(Icons.Filled.Search, "Rechercher")
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

                        // Texte d'affichage
                        Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                            val selectedFormats = mutableListOf<String>().apply {
                                if (selectedOptions[0]) add("xlsx")
                                if (selectedOptions[1]) add("csv")
                            }
                            val textExtension = if (selectedFormats.isNotEmpty()) {selectedFormats.joinToString(" et ") {"$exportFolderPath\\$exportFileName.$it"}} else {""}
                            val formattedPath = when {
                                exportFolderPath.isEmpty() && exportFileName.isEmpty() && selectedFormats.isEmpty() -> "Aucun emplacement, nom ou type de fichier sélectionné"
                                exportFolderPath.isEmpty() && exportFileName.isEmpty() -> "Aucun emplacement ou nom de fichier sélectionné"
                                exportFolderPath.isEmpty() && selectedFormats.isEmpty() -> "Aucun emplacement ou type de fichier sélectionné"
                                exportFileName.isEmpty() && selectedFormats.isEmpty() -> "Aucun nom ou type de fichier sélectionné"
                                exportFolderPath.isEmpty() -> "Aucun emplacement sélectionné"
                                exportFileName.isEmpty() -> "Aucun nom de fichier sélectionné"
                                selectedFormats.isEmpty() -> "Aucun type de fichier sélectionné"
                                else -> "Le fichier sera exporté sous : $textExtension"
                            }
                            val textColor = if (!formattedPath.contains("Aucun")) {Color.Green.copy(0.5f)} else {Color.Red}
                            Text(formattedPath, color = textColor, fontSize = 15.sp, style = TextStyle(lightGray, textAlign = TextAlign.Center))
                        }
                    }

                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)

                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Bouton d'annulation
                        Button(onDialogWindowDismissRequest, Modifier.weight(1f), enabled = true, elevation = ButtonDefaults.elevation(10.dp), shape = RoundedCornerShape(100), colors = getButtonColors(middleGray, darkGray, lightGray)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Close, "")
                                Spacer(Modifier.width(10.dp))
                                Text("Annuler")
                            }
                        }

                        Spacer(Modifier.weight(0.1f))

                        // Bouton d'exportation
                        val isValidFolderPath = exportFolderPath.matches(Regex("[A-Za-z]:\\\\.*"))
                        val hasSelectedFormat = selectedOptions[0] || selectedOptions[1]
                        Button(
                            onClick = {onExport(exportFolderPath.toString(), exportFileName, selectedOptions)},
                            modifier = Modifier.weight(1f),
                            enabled = exportFolderPath.isNotBlank() && !java.io.File(exportFolderPath).exists() && isValidFolderPath && exportFileName.isNotBlank() && hasSelectedFormat,
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