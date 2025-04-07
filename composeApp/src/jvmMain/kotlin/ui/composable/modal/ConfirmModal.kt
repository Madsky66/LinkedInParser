package ui.composable.modal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import config.GlobalInstance.config as gC
import ui.composable.element.SpacedDivider
import utils.getButtonColors


@Composable
fun ConfirmModal(string: String = "", modalMessage: String, firstButtonText: String, mainButtonText: String, secondButtonText: String = "", onMainButtonTap: (String) -> Unit) {
    val dialogState = rememberDialogState(size = DpSize(640.dp, 360.dp))
    val darkGray = gC.darkGray.value
    val middleGray = gC.middleGray.value
    val lightGray = gC.lightGray.value

    DialogWindow({gC.showConfirmModal.value = true}, state = dialogState, transparent = true, undecorated = true) {
        WindowDraggableArea(Modifier.fillMaxSize().shadow(5.dp)) {
            Card(Modifier, RectangleShape, backgroundColor = middleGray, contentColor = lightGray, BorderStroke(1.dp, darkGray), elevation = 5.dp) {
                Column(Modifier.padding(20.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                    // Barre de titre
                    Column(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                            // Titre et icone
                            Row(Modifier, Arrangement.Start, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, "", Modifier.size(30.dp))
                                Spacer(Modifier.width(20.dp))
                                Text(text = "Confirmation", fontSize = 25.sp)
                            }
                            // Bouton de fermeture
                            Row(Modifier, Arrangement.End, Alignment.CenterVertically) {IconButton({gC.showConfirmModal.value = true}) {Icon(Icons.Filled.Close, "Quitter")}}
                        }
                        SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)
                    }
                    // Message
                    Row(Modifier.weight(1f, true).fillMaxWidth().background(darkGray.copy(0.5f)).border(1.dp, darkGray), Arrangement.Center, Alignment.CenterVertically) {Text(modalMessage, fontSize = 15.sp, textAlign = TextAlign.Center)}
                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)
                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Bouton d'annulation
                        Button({gC.showConfirmModal.value = true}, Modifier.weight(1f), true, elevation = ButtonDefaults.elevation(10.dp), shape = RoundedCornerShape(100), colors = getButtonColors(middleGray, darkGray, lightGray)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Close, "")
                                Spacer(Modifier.width(10.dp))
                                Text(firstButtonText)
                            }
                        }
                        // Spacer
                        Spacer(Modifier.weight(0.1f))
                        // Bouton principal
                        Button({onMainButtonTap(string)}, Modifier.weight(1f), true, elevation = ButtonDefaults.elevation(10.dp), shape = RoundedCornerShape(100), colors = getButtonColors(middleGray, darkGray, lightGray)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Check, "")
                                Spacer(Modifier.width(10.dp))
                                Text(mainButtonText)
                            }
                        }
                    }
                }
            }
        }
    }
}