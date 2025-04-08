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
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import config.GlobalInstance.config as gC
import kotlinx.coroutines.CoroutineScope
import ui.composable.element.SpacedDivider
import ui.composable.effect.EllipsisVisualTransformation
import ui.composable.openDialog
import utils.*
import java.io.File

fun onImportModalClose() {
    gC.consoleMessage.value = ConsoleMessage("⚠️ Importation annulée", ConsoleMessageType.WARNING)
    gC.showImportModal.value = false
    gC.isWaitingForSelection.value = false
}
fun onImportConfirm(applicationScope: CoroutineScope) {
    gC.fileImportManager.importFromFile(applicationScope)
    gC.showImportModal.value = false
    gC.isWaitingForSelection.value = false
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileImportModal(applicationScope: CoroutineScope) {
    val dialogState = rememberDialogState(size = DpSize(640.dp, 360.dp))
    gC.isWaitingForSelection.value = true

    LaunchedEffect(gC.fileInstance.value) {
        if (gC.fileInstance.value != null && gC.isWaitingForSelection.value) {
            gC.fileFullPath.value = gC.fileInstance.value!!.path
            gC.filePath.value = "${gC.fileFullPath.value.substringBeforeLast("\\")}\\"
            gC.fileName.value = gC.fileFullPath.value.substringAfterLast("\\").split(".").first()
            gC.fileFormat.value = gC.fileFullPath.value.substringAfterLast('.', "").lowercase()
        }
        print("\n---\n${gC.fileFullPath.value} | ${gC.filePath.value} | ${gC.fileName.value} | ${gC.fileFormat.value}")
    }

    val isPathCorrect = (gC.filePath.value.matches(Regex("[A-Za-z]:\\\\.*")) == true) && (gC.fileName.value != "") && (gC.fileFormat.value.lowercase() == "xlsx")
    val formatColor = when (gC.fileFormat.value) {
        "" -> gC.lightGray.value
        "xlsx" -> Color.Green.copy(0.5f)
        else -> Color.Red.copy(0.5f)
    }

    DialogWindow({onImportModalClose()}, dialogState, transparent = true, undecorated = true) {
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
                            Row(Modifier, Arrangement.End, Alignment.CenterVertically) {IconButton({onImportModalClose()}) {Icon(Icons.Filled.Close, "Quitter")}}
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
                                Box(Modifier.widthIn(min = 50.dp).background(gC.darkGray.value, RoundedCornerShape(50)).padding(10.dp, 5.dp), Alignment.Center) {Text(gC.fileFormat.value, color = formatColor, fontSize = 17.sp)}
                            }

                            // Spacer
                            Spacer(Modifier.height(5.dp))

                            // Zone du chemin d 'importation
                            OutlinedTextField(
                                value = gC.fileFullPath.value,
                                onValueChange = {gC.fileFullPath.value = it},
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
                                    IconButton({gC.fileInstance.value = File(openDialog("Sélectionner un fichier à importer...") ?: "")}, Modifier.size(25.dp).align(Alignment.CenterHorizontally)) {
                                        Icon(Icons.Filled.Search, "Rechercher", tint = gC.lightGray.value)
                                    }
                                },
                                visualTransformation = EllipsisVisualTransformation()
                            )
                        }
                    }

                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(gC.darkGray.value.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)

                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Bouton d'annulation
                        Button(
                            onClick = {onImportModalClose()},
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

                        // Spacer
                        Spacer(Modifier.weight(0.1f))

                        // Bouton d'importation
                        Button(
                            onClick = {onImportConfirm(applicationScope)},
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