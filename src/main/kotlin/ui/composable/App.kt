import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    var text by remember {mutableStateOf("")}
    var result by remember {mutableStateOf("En attente...")}
    var isLoading by remember {mutableStateOf(false)}

    MaterialTheme {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("URL LinkedIn") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        isLoading = true
                        sendToPython()
                        result = checkResults("src/main/data/data.json")
                        isLoading = false
                    }
                    else {result = "❌ Veuillez entrer une URL."}
                },
                enabled = !isLoading && text.isNotBlank()
            ) {Text("Envoyer URL")}

            Spacer(Modifier.height(10.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else {Text(result, Modifier.padding(top = 10.dp))}
        }
    }
}