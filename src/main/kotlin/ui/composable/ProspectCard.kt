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
import androidx.compose.ui.unit.dp
import data.ProspectData

@Composable
fun ProspectCard(prospect: ProspectData?, themeColors: List<Color>, isImportationLoading: Boolean, isExportationLoading: Boolean) {
    val (darkGray, middleGray, lightGray) = themeColors
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(10), elevation = 3.dp, backgroundColor = darkGray) {
        if (isImportationLoading || isExportationLoading) {Box(Modifier.fillMaxWidth().padding(20.dp).size(50.dp)) {CircularProgressIndicator(Modifier.align(Alignment.TopEnd), lightGray, strokeWidth = 5.dp)}}
        Column(Modifier.padding(25.dp, 40.dp).fillMaxWidth(), Arrangement.Center, Alignment.Start) {
            Row(Modifier, Arrangement.Start, Alignment.CenterVertically) {
                Icon(Icons.Default.Person, "Contact", Modifier.size(50.dp), lightGray)
                Spacer(Modifier.width(10.dp))
                Column(Modifier, Arrangement.Center, Alignment.Start) {
                    val displayName = when {
                        prospect == null -> "Aucun profil sélectionné"
                        prospect.fullName.isBlank() -> {
                            val firstName = if (prospect.firstName.isBlank() || prospect.firstName == "null") "Prénom inconnu" else prospect.firstName
                            val lastName = if (prospect.lastName.isBlank() || prospect.lastName == "null") "Nom inconnu" else prospect.lastName
                            "$firstName $lastName"
                        }
                        else -> prospect.fullName
                    }

                    Text(displayName, color = lightGray, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))

                    val displayEmail = when {
                        prospect == null -> "Aucun email"
                        prospect.email.isBlank() || prospect.email == "null" -> "Email inconnu"
                        else -> prospect.email
                    }
                    Text(displayEmail, color = lightGray, style = MaterialTheme.typography.body1)
                }
            }
            Spacer(Modifier.height(20.dp))

            val displayCompany = when {
                prospect == null -> "Aucune entreprise"
                prospect.company.isBlank() || prospect.company == "null" -> "Entreprise inconnue"
                else -> prospect.company
            }
            Text(displayCompany, color = lightGray, style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(5.dp))

            val displayJobTitle = when {
                prospect == null -> "Aucun poste"
                prospect.jobTitle.isBlank() || prospect.jobTitle == "null" -> "Poste inconnu"
                else -> prospect.jobTitle
            }
            Text(displayJobTitle, color = lightGray, style = MaterialTheme.typography.body2)
        }
    }
}