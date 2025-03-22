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
import manager.GoogleSheetsManager
import manager.sendToPythonOverWebSocket
import ui.composable.ProspectCard

@Composable
fun App() {
    var linkedinUrl by remember {mutableStateOf("")}
    var prospects by remember {mutableStateOf(listOf<ProspectData>())}
    var isLoading by remember {mutableStateOf(false)}
    var errorMessage by remember {mutableStateOf("")}

    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colors.background)) {
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
                                val prospect = ProspectData(linkedinURL = linkedinUrl, status = "completed")
                                prospects = prospects + prospect
                                try {GoogleSheetsManager().saveProspect(prospect)} catch (e: Exception) {errorMessage = "⚠️ Erreur lors de la sauvegarde dans Google Sheets"}
                                linkedinUrl = ""
                            }
                            else {errorMessage = result}
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && linkedinUrl.isNotBlank()
            ) {
                if (isLoading) {CircularProgressIndicator(color = MaterialTheme.colors.onPrimary)}
                else {Text("Analyser le profil")}
            }
            if (errorMessage.isNotEmpty()) {Text(errorMessage, Modifier.padding(vertical = 8.dp), color = MaterialTheme.colors.error)}
            Spacer(Modifier.height(16.dp))
            LazyColumn {items(prospects) {prospect -> ProspectCard(prospect)}}
        }
    }
}