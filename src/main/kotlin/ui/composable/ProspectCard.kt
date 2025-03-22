package ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.ProspectData
import kotlin.text.ifEmpty

@Composable
fun ProspectCard(prospect: ProspectData) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 4.dp) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Text(text = prospect.fullName.ifEmpty {"Nom inconnu"}, style = MaterialTheme.typography.h6)
            Text(text = prospect.email.ifEmpty {"Email non trouvé"}, style = MaterialTheme.typography.body1)
            Text(text = prospect.company.ifEmpty {"Entreprise inconnue"}, style = MaterialTheme.typography.body2)
            LinearProgressIndicator(progress = if (prospect.status == "completed") 1f else 0.5f, Modifier.fillMaxWidth().padding(vertical = 8.dp))
        }
    }
}