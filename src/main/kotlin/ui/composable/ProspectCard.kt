package ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import config.GlobalConfig

@Composable
fun ProspectCard(gC: GlobalConfig) {
    val prospect = gC.currentProfile.value
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(10), elevation = 3.dp, backgroundColor = gC.darkGray.value) {
        if (gC.isImportationLoading.value || gC.isExportationLoading.value) {Box(Modifier.fillMaxWidth().padding(15.dp)) {CircularProgressIndicator(Modifier.size(25.dp).align(Alignment.TopEnd), gC.lightGray.value, strokeWidth = 5.dp)}}

        Column(Modifier.fillMaxWidth()) {
            // Partie principale
            Row(Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp), Arrangement.Start, Alignment.CenterVertically) {

                // Icone du profil
                Icon(Icons.Default.Person, "Contact", Modifier.size(100.dp), gC.lightGray.value)

                // Spacer
                Spacer(Modifier.width(10.dp))

                Column(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.Start) {
                    // Nom et prénom
                    val displayName = when {
                        prospect == null -> "Aucun profil sélectionné"
                        prospect.fullName.isBlank() -> {
                            val firstName = if (prospect.firstName.isBlank() || prospect.firstName == "null") "Prénom inconnu" else prospect.firstName
                            val lastName = if (prospect.lastName.isBlank() || prospect.lastName == "null") "Nom inconnu" else prospect.lastName
                            "$firstName $lastName"
                        }
                        else -> prospect.fullName
                    }
                    Text(displayName, color = gC.lightGray.value, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                    //Spacer
                    Spacer(Modifier.height(5.dp))
                    // Mail
                    val displayEmail = when {
                        prospect == null -> "Aucun email"
                        prospect.email.isBlank() || prospect.email == "null" -> "Email inconnu"
                        else -> prospect.email
                    }
                    Text(displayEmail, color = gC.lightGray.value, style = MaterialTheme.typography.body1)
                }
            }

            // Spacer
            Spacer(Modifier.height(10.dp))

            // Partie secondaire
            Column(Modifier.fillMaxWidth().padding(25.dp, 0.dp, 0.dp, 25.dp), Arrangement.Center, Alignment.Start) {
                // Entreprise
                val displayCompany = when {
                    prospect == null -> "Aucune entreprise"
                    prospect.company.isBlank() || prospect.company == "null" -> "Entreprise inconnue"
                    else -> prospect.company
                }
                Text(displayCompany, color = gC.lightGray.value, style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
                // Spacer
                Spacer(Modifier.height(5.dp))
                // Poste
                val displayJobTitle = when {
                    prospect == null -> "Aucun poste"
                    prospect.jobTitle.isBlank() || prospect.jobTitle == "null" -> "Poste inconnu"
                    else -> prospect.jobTitle
                }
                Text(displayJobTitle, color = gC.lightGray.value, style = MaterialTheme.typography.body2)
            }
        }
    }
}