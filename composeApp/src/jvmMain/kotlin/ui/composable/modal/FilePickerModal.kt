package ui.composable.modal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow

@Composable
fun FilePickerModal(spreadsheets: List<Pair<String, String>>, onFileSelected: (String) -> Unit, onDismiss: () -> Unit) {
    DialogWindow(onDismiss, title = "Choisir un Google Sheets") {
        Column(Modifier.padding(16.dp)) {
            Text("Sélectionner une feuille :")
            Spacer(Modifier.height(8.dp))
            for ((id, name) in spreadsheets) {
                Button({onFileSelected(id)}, Modifier.fillMaxWidth().padding(vertical = 4.dp)) {Text(name)}
            }
            Spacer(Modifier.height(8.dp))
            Button(onDismiss, Modifier.align(Alignment.End)) {Text("Annuler")}
        }
    }
}
