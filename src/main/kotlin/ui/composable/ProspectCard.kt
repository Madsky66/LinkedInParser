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
fun ProspectCard(prospect: ProspectData, themeColors: List<Color>, isImportationLoading: Boolean, isExportationLoading: Boolean) {
    val (darkGray, middleGray, lightGray) = themeColors
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(10), elevation = 3.dp, backgroundColor = darkGray) {
        Column(Modifier.padding(25.dp, 20.dp).fillMaxWidth(), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
            if (isImportationLoading ||isExportationLoading) {CustomProgressIndicator(themeColors)} else {
                Text(prospect.fullName.ifEmpty {"Nom inconnu"}, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold, color = lightGray)
                Spacer(Modifier.height(5.dp))
                Text(prospect.email.ifEmpty {"Email non trouvé"}, style = MaterialTheme.typography.body1, color = lightGray)
                Spacer(Modifier.height(20.dp))
                Text(prospect.company.ifEmpty {"Entreprise inconnue"}, style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold, color = lightGray)
                Spacer(Modifier.height(5.dp))
                Text(prospect.jobTitle.ifEmpty {"Titre non trouvé"}, style = MaterialTheme.typography.body2, color = lightGray)
            }
        }
    }
}

@Composable
fun EmptyProspectCard(themeColors: List<Color>, isImportationLoading: Boolean, isExportationLoading: Boolean) {
    val (darkGray, middleGray, lightGray) = themeColors
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(10), elevation = 3.dp, backgroundColor = darkGray) {
        Column(Modifier.padding(25.dp, 40.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (isImportationLoading ||isExportationLoading) {CustomProgressIndicator(themeColors)} else {
                Icon(Icons.Default.Person, contentDescription = "Contact", Modifier.size(50.dp), tint = lightGray)
                Spacer(Modifier.height(10.dp))
                Text("Aucun profil sélectionné", style = MaterialTheme.typography.h6, textAlign = TextAlign.Center, color = lightGray)
                Spacer(Modifier.height(5.dp))
                Text("Entrez une URL LinkedIn pour commencer", style = MaterialTheme.typography.body2, textAlign = TextAlign.Center, color = lightGray.copy(0.5f))
            }
        }
    }
}