package ui.composable.element

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import config.GlobalConfig
import utils.getButtonColors


@Composable
fun MultiChoiceSegmentedButton(gC: GlobalConfig, width: Float = 1f) {
    val switchXLSXBackgroundColor = if (gC.selectedOptions[0]) {gC.darkGray.value.copy(0.5f)} else {gC.middleGray.value}
    val switchCSVBackgroundColor = if (gC.selectedOptions[1]) {gC.darkGray.value.copy(0.5f)} else {gC.middleGray.value}

    // Boutons
    Row(Modifier.fillMaxWidth(width).clip(RoundedCornerShape(100))) {
        Button(
            onClick = {gC.selectedOptions[0] = !gC.selectedOptions[0]},
            modifier = Modifier.weight(1f),
            enabled = true,
            elevation = ButtonDefaults.elevation(10.dp),
            shape = RoundedCornerShape(100, 0, 0, 100),
            border = BorderStroke(1.dp, gC.darkGray.value),
            colors = getButtonColors(switchXLSXBackgroundColor, gC.darkGray.value, gC.lightGray.value)
        ) {Text("XLSX")}
        Button(
            onClick = {gC.selectedOptions[1] = !gC.selectedOptions[1]},
            modifier = Modifier.weight(1f),
            enabled = true,
            elevation = ButtonDefaults.elevation(10.dp),
            shape = RoundedCornerShape(0, 100, 100, 0),
            border = BorderStroke(1.dp, gC.darkGray.value),
            colors = getButtonColors(switchCSVBackgroundColor, gC.darkGray.value, gC.lightGray.value)
        ) {Text("CSV")}
    }
}