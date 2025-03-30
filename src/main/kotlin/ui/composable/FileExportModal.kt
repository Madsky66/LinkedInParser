package ui.composable

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import utils.FileFormat

@Composable
fun FileExportModal(themeColors: List<Color>, onExport: (filePath: String, format: FileFormat) -> Unit, onDialogWindowDismissRequest: () -> Unit) {
    var filePath by remember {mutableStateOf("")}
    var exportFileFormat by remember {mutableStateOf(FileFormat.CSV)}
    val (darkGray, middleGray, lightGray) = themeColors
    val dialogState = rememberDialogState(size = DpSize(640.dp, 360.dp))
    val selectedOptions = rememberSaveable {mutableStateListOf(false, false)}

    DialogWindow(onDialogWindowDismissRequest, dialogState, transparent = true, undecorated = true) {
        WindowDraggableArea(Modifier.fillMaxSize().shadow(5.dp)) {
            Card(Modifier, shape = RectangleShape, backgroundColor = middleGray, contentColor = lightGray, border = BorderStroke(1.dp, darkGray), elevation = 5.dp) {
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
                            Row(Modifier, Arrangement.End, Alignment.CenterVertically) {IconButton({onDialogWindowDismissRequest}) {Icon(Icons.Filled.Close, "Quitter")}}
                        }
                        SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)
                    }
                    // Contenu
                    Row(Modifier.weight(1f, true).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Zone de texte
                        Column(Modifier.weight(0.525f), Arrangement.Center, Alignment.CenterHorizontally) {
                            Column(Modifier.padding(10.dp, 0.dp).fillMaxWidth(), Arrangement.Center, Alignment.Start) {Text("Format(s) d'exportation :", color = lightGray, fontSize = 20.sp)}
                            Spacer(Modifier.height(5.dp))
                            OutlinedTextField(
                                value = filePath,
                                onValueChange = {filePath = it},
                                label = {Text("Sélectionner un emplacement...")},
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
                                        onClick = {try {filePath = openDialog("Sélectionner un emplacement...").toString()} catch (e: Exception) {print("Erreur : $e")}},
                                        modifier = Modifier.size(25.dp).align(Alignment.CenterHorizontally)
                                    ) {
                                        Icon(Icons.Filled.Search, "Rechercher")
                                    }
                                }
                            )
                        }
                        // Spacer
                        Spacer(Modifier.fillMaxWidth(0.05f))
                        // Choix du format
                        Column(Modifier.weight(0.425f), Arrangement.Center, Alignment.CenterHorizontally) {
                            Column(Modifier.padding(10.dp, 0.dp).fillMaxWidth(), Arrangement.Center, Alignment.Start) {Text("Format(s) d'exportation :", color = lightGray, fontSize = 20.sp)}
                            Spacer(Modifier.height(5.dp))
                            MultiChoiceSegmentedButton(themeColors, selectedOptions) {format -> exportFileFormat = format}
                        }
                    }
                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)
                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
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
                        Button(
                            onClick = {onExport(filePath, exportFileFormat)},
                            modifier = Modifier.weight(1f),
                            enabled = true,
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