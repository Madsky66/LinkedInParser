package ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import utils.ExportFormat


@Composable
fun ExportModal(onExport: (filePath: String, format: ExportFormat) -> Unit, onDismissRequest: () -> Unit) {
    var filePath by remember {mutableStateOf("")}
    var selectedFormat by remember {mutableStateOf(ExportFormat.CSV)}

    DialogWindow(onCloseRequest = onDismissRequest) {
        Surface(shape = RoundedCornerShape(50.dp), color = Color.White) {
            Column(Modifier.padding(16.dp)) {
                Text("Exporter", fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = filePath,
                    onValueChange = {filePath = it},
                    label = {Text("Chemin du fichier")},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Format:")
                Row(Modifier.fillMaxWidth()) {
                    RadioButton(selected = (selectedFormat == ExportFormat.CSV), onClick = {selectedFormat = ExportFormat.CSV})
                    Text("CSV", Modifier.align(Alignment.CenterVertically))
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = (selectedFormat == ExportFormat.XLSX), onClick = {selectedFormat = ExportFormat.XLSX})
                    Text("XLSX", Modifier.align(Alignment.CenterVertically))
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.End) {
                    Button(onClick = onDismissRequest) {Text("Annuler")}
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {onExport(filePath, selectedFormat)}) {Text("Exporter")}
                }
            }
        }
    }
}