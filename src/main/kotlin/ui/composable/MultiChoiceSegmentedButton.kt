package ui.composable

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import utils.FileFormat


@Composable
fun MultiChoiceSegmentedButton(themeColors: List<Color>, selectedOptions: MutableList<Boolean>, onFormatSelected: (FileFormat) -> Unit) {
    val (darkGray, middleGray, lightGray) = themeColors
    val switchLeftBackgroundColor = if (selectedOptions[0]) {darkGray.copy(0.5f)} else {middleGray}
    val switchRightBackgroundColor = if (selectedOptions[1]) {darkGray.copy(0.5f)} else {middleGray}

    // Boutons
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(100))) {
        Button(
            onClick = {selectedOptions[0] = !selectedOptions[0]},
            modifier = Modifier.weight(1f),
            enabled = true,
            elevation = ButtonDefaults.elevation(10.dp),
            shape = RoundedCornerShape(100, 0, 0, 100),
            border = BorderStroke(1.dp, darkGray),
            colors = getButtonColors(switchLeftBackgroundColor, darkGray, lightGray)
        ) {Text("CSV")}
        Button(
            onClick = {selectedOptions[1] = !selectedOptions[1]},
            modifier = Modifier.weight(1f),
            enabled = true,
            elevation = ButtonDefaults.elevation(10.dp),
            shape = RoundedCornerShape(0, 100, 100, 0),
            border = BorderStroke(1.dp, darkGray),
            colors = getButtonColors(switchRightBackgroundColor, darkGray, lightGray)
        ) {Text("XLSX")}
    }
}