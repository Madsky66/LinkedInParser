package ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.ProspectData
import kotlin.text.ifEmpty
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ProspectCard(prospect: ProspectData) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 4.dp) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Text(prospect.fullName.ifEmpty {"Nom inconnu"}, style = MaterialTheme.typography.h6)
            Text(prospect.email.ifEmpty {"Email non trouvé"}, style = MaterialTheme.typography.body1)
            Text(prospect.company.ifEmpty {"Entreprise inconnue"}, style = MaterialTheme.typography.body2)
            LinearProgressIndicator(progress = if (prospect.status == "completed") 1f else 0.5f, Modifier.fillMaxWidth().padding(vertical = 8.dp))
        }
    }
}

@Composable
fun EmptyProspectCard() {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 4.dp, backgroundColor = MaterialTheme.colors.surface) {
        Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Contact",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colors.onSurface.copy(0.6f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Aucun profil sélectionné",
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onSurface.copy(0.6f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Entrez une URL LinkedIn pour commencer",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onSurface.copy(0.4f)
            )
        }
    }
}