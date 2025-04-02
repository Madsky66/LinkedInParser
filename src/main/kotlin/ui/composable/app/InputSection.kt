package ui.composable.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RowScope.InputSection(applicationScope: CoroutineScope, pastedInput: String, isLoading: Boolean, themeColors: List<Color>, onInputChange: (String) -> Unit, onProcessInput: (String) -> Unit) {
    val (darkGray, middleGray, lightGray) = themeColors
    Column(Modifier.weight(1.75f).fillMaxHeight().padding(bottom = 5.dp), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = pastedInput,
            onValueChange = {
                applicationScope.launch {
                    onInputChange(it)
                    if (!isLoading) onProcessInput(it)
                }
            },
            label = {Text("Coller le texte de la page LinkedIn ici...")},
            modifier = Modifier.weight(0.9f).fillMaxWidth().clip(RectangleShape),
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
}