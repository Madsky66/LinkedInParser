package ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import readJson
import sendToPython

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
                    result = checkResults("src/main/data/data.json").toString()
                    isLoading = false
                }
                else {result = "❌ Veuillez entrer une URL."}
            },
                enabled = !isLoading) {Text("Envoyer URL")}

            Spacer(Modifier.height(10.dp))

            if (isLoading) {CircularProgressIndicator()}
            else {
                Text(result, Modifier.padding(top = 10.dp))
                if (result.contains("En attente")) {Button(onClick = {result = checkResults("src/main/data/data.json").toString()}) {Text("🔄 Vérifier à nouveau")}}
            }
        }
    }
}

fun checkResults(filePath: String) {
    val processedData = readJson(filePath)
    if (processedData.status == "completed") {println("✅ Résultats obtenus : Nom = ${processedData.name}, Email = ${processedData.email}")}
    else {println("⏳ En attente du traitement Python...")}
}