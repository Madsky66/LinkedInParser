package ui.composable.modal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import config.GlobalConfig
import ui.composable.element.SpacedDivider
import utils.getButtonColors


@Composable
fun ConfirmModal(gC: GlobalConfig, string: String = "", modalMessage: String, firstButtonText: String, secondButtonText: String, thirdButtonText: String = "", onSecondButtonClick: (String) -> Unit, onDismissRequest: () -> Unit) {
    val dialogState = rememberDialogState(position = WindowPosition.PlatformDefault, size = DpSize(640.dp, 360.dp))

    DialogWindow(onDismissRequest, state = dialogState, transparent = true, undecorated = true) {
        WindowDraggableArea(Modifier.fillMaxSize().shadow(5.dp)) {
            Card(Modifier, shape = RectangleShape, backgroundColor = gC.middleGray.value, contentColor = gC.lightGray.value, border = BorderStroke(1.dp, gC.darkGray.value), elevation = 5.dp) {
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
                            Row(Modifier, Arrangement.End, Alignment.CenterVertically) {IconButton(onDismissRequest) {Icon(Icons.Filled.Close, "Quitter")}}
                        }
                        SpacedDivider(Modifier.fillMaxWidth().background(gC.darkGray.value.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)
                    }
                    // Message
                    Row(Modifier.weight(1f, true).fillMaxWidth().background(gC.darkGray.value.copy(0.5f)).border(1.dp, gC.darkGray.value), Arrangement.Center, Alignment.CenterVertically) {
                        Text(modalMessage, fontSize = 15.sp, textAlign = TextAlign.Start)
                    }
                    // Diviseur espacé
                    SpacedDivider(Modifier.fillMaxWidth().background(gC.darkGray.value.copy(0.5f)), "vertical", 1.dp, 20.dp, 20.dp)
                    // Boutons
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Button(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            elevation = ButtonDefaults.elevation(10.dp),
                            shape = RoundedCornerShape(100),
                            colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
                        ) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Close, "")
                                Spacer(Modifier.width(10.dp))
                                Text(firstButtonText)
                            }
                        }
                        Spacer(Modifier.weight(0.1f))
                        Button(
                            onClick = {onSecondButtonClick(string)},
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            elevation = ButtonDefaults.elevation(10.dp),
                            shape = RoundedCornerShape(100),
                            colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
                        ) {
                            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                                Icon(Icons.Filled.Check, "")
                                Spacer(Modifier.width(10.dp))
                                Text(secondButtonText)
                            }
                        }
                    }
                }
            }
        }
    }
}