package ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.ProspectData

@Composable
fun ProspectCard(prospect: ProspectData, COLOR_PRIMARY: Color, COLOR_SECONDARY: Color) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(10), elevation = 3.dp, backgroundColor = COLOR_PRIMARY) {
        Column(Modifier.padding(25.dp, 20.dp).fillMaxWidth(), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
            Text(prospect.fullName.ifEmpty {"Nom inconnu"}, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold, color = COLOR_SECONDARY)
            Spacer(Modifier.height(5.dp))
            Text(prospect.email.ifEmpty {"Email non trouvé"}, style = MaterialTheme.typography.body1, color = COLOR_SECONDARY)
            Spacer(Modifier.height(20.dp))
            Text(prospect.company.ifEmpty {"Entreprise inconnue"}, style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold, color = COLOR_SECONDARY)
            Spacer(Modifier.height(5.dp))
            Text(prospect.jobTitle.ifEmpty {"Titre non trouvé"}, style = MaterialTheme.typography.body2, color = COLOR_SECONDARY)
        }
    }
}

@Composable
fun EmptyProspectCard(COLOR_PRIMARY: Color, COLOR_SECONDARY: Color) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(10), elevation = 3.dp, backgroundColor = COLOR_PRIMARY) {
        Column(Modifier.padding(25.dp, 40.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Person, contentDescription = "Contact", Modifier.size(50.dp), tint = COLOR_SECONDARY)
            Spacer(Modifier.height(10.dp))
            Text("Aucun profil sélectionné", style = MaterialTheme.typography.h6, textAlign = TextAlign.Center, color = COLOR_SECONDARY)
            Spacer(Modifier.height(5.dp))
            Text("Entrez une URL LinkedIn pour commencer", style = MaterialTheme.typography.body2, textAlign = TextAlign.Center, color = COLOR_SECONDARY.copy(0.5f))
        }
    }
}