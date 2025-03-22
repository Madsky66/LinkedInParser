import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.ProspectData
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import manager.sendToPythonOverWebSocket

@Composable
fun App() {
    var linkedinUrl by remember {mutableStateOf("")}
    var prospects by remember {mutableStateOf(listOf<ProspectData>())}
    var isLoading by remember {mutableStateOf(false)}
    var errorMessage by remember {mutableStateOf("")}

    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colors.background)) {
            Text("LinkedIn Parser Pro", Modifier.padding(bottom = 16.dp), style = MaterialTheme.typography.h5)
            OutlinedTextField(
                value = linkedinUrl,
                onValueChange = {linkedinUrl = it},
                modifier = Modifier.fillMaxWidth(),
                label = {Text("URL LinkedIn")},
                leadingIcon = {Icon(Icons.Default.Search, "LinkedIn URL")}
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (linkedinUrl.isNotBlank()) {
                        isLoading = true
                        errorMessage = ""
                        sendToPythonOverWebSocket(ProspectData(linkedinURL = linkedinUrl)) {result ->
                            isLoading = false
                            if (result.startsWith("✅")) {
                                prospects = prospects + ProspectData(linkedinURL = linkedinUrl, status = "completed")
                                linkedinUrl = ""
                            }
                            else {errorMessage = result}
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && linkedinUrl.isNotBlank()
            ) {
                if (isLoading) {CircularProgressIndicator(color = MaterialTheme.colors.onPrimary)} else {Text("Analyser le profil")}
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, Modifier.padding(vertical = 8.dp), color = MaterialTheme.colors.error)}

            Spacer(Modifier.height(16.dp))

            LazyColumn {items(prospects) {prospect -> ProspectCard(prospect)}}
        }
    }
}

@Composable
fun ProspectCard(prospect: ProspectData) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 4.dp) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Text(text = prospect.name.ifEmpty {"Nom inconnu"}, style = MaterialTheme.typography.h6)
            Text(text = prospect.email.ifEmpty {"Email non trouvé"}, style = MaterialTheme.typography.body1)
            Text(text = prospect.company.ifEmpty {"Entreprise inconnue"}, style = MaterialTheme.typography.body2)
            LinearProgressIndicator(progress = if (prospect.status == "completed") 1f else 0.5f, Modifier.fillMaxWidth().padding(vertical = 8.dp))
        }
    }
}