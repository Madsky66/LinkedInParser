import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
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
    var errorMessage by remember {mutableStateOf<String?>(null)}

    MaterialTheme {
        Column(Modifier.padding(16.dp).fillMaxSize()) {
            OutlinedTextField(
                value = text,
                onValueChange = {text = it},
                label = {Text("URL LinkedIn")},
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null
            )
            errorMessage?.let {Text(it, Modifier.padding(top = 8.dp), MaterialTheme.colors.error)}
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        isLoading = true
                        errorMessage = null
                        sendToPythonOverWebSocket(ProspectData(linkedinURL = text)) {response ->
                            if (response.startsWith("✅")) {result = response} else {errorMessage = response}
                            isLoading = false
                        }
                    }
                    else {errorMessage = "❌ Veuillez entrer une URL."}
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank() && !isLoading
            ) {Text("Envoyer URL")}
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