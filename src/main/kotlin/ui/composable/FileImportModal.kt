package ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import utils.FileFormat


@Composable
fun FileImportModal(themeColors: List<Color>, onImportFile: (filePath: String?, format: FileFormat) -> Unit, onDismissRequest: () -> Unit) {
    var importFilePath by remember {mutableStateOf<String?>(null)}
    var importFileFormat by remember {mutableStateOf(FileFormat.CSV)}
    val (darkGray, middleGray, lightGray) = themeColors

    DialogWindow(onDismissRequest, transparent = true, undecorated = true) {
        WindowDraggableArea(Modifier.fillMaxSize().shadow(5.dp)) {
            Card(Modifier, shape = RectangleShape, backgroundColor = middleGray, contentColor = lightGray, border = BorderStroke(1.dp, darkGray), elevation = 5.dp) {
                Column(Modifier.padding(20.dp)) {
                }
            }
        }
    }
}