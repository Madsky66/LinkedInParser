import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.ProspectData
import manager.sendToPythonOverWebSocket

@Composable
fun App() {
    var text by remember {mutableStateOf("")}
    var result by remember {mutableStateOf("En attente...")}
    var isLoading by remember {mutableStateOf(false)}

    MaterialTheme {
        Column(Modifier.padding(16.dp).fillMaxSize().background(MaterialTheme.colors.background)) {
            OutlinedTextField(
                value = text,
                onValueChange = {text = it},
                label = {Text("URL LinkedIn")},
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                leadingIcon = {Icon(Icons.Default.Search, "LinkedIn URL")},
                shape = MaterialTheme.shapes.large
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        isLoading = true
                        sendToPythonOverWebSocket(ProspectData(linkedinURL = text)) { response ->
                            result = response
                            isLoading = false
                        }
                    }
                    else {result = "❌ Veuillez entrer une URL."}
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp).height(56.dp),
                enabled = !isLoading && text.isNotBlank(),
                shape = MaterialTheme.shapes.medium) {if (isLoading) {CircularProgressIndicator(Modifier.size(24.dp))} else {Text("Envoyer URL")}
            }
            Spacer(Modifier.height(16.dp))

            if (isLoading) {Box(Modifier.fillMaxSize(), Alignment.Center) {CircularProgressIndicator()}}
            else {
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Résultat : ", Modifier.weight(1f))
                        Text(result)
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("URL : ", Modifier.weight(1f))
                        Text(text)
                    }
                }
            }
        }
    }
}