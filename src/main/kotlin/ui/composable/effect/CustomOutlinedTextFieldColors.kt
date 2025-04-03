package ui.composable.effect

import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import config.GlobalConfig

@Composable
fun CustomOutlinedTextFieldColors(gC: GlobalConfig): TextFieldColors {
    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = gC.lightGray.value,
        focusedBorderColor = gC.lightGray.value.copy(0.25f),
        unfocusedBorderColor = gC.lightGray.value.copy(0.15f),
        focusedLabelColor = gC.lightGray.value.copy(0.5f),
        unfocusedLabelColor = gC.lightGray.value.copy(0.5f),
        placeholderColor = gC.lightGray.value.copy(0.25f)
    )
    return textFieldColors
}