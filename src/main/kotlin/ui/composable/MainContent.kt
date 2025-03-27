import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import data.ProspectData
import manager.LinkedInManager
import org.slf4j.LoggerFactory
import ui.composable.EmptyProspectCard
import ui.composable.ProspectCard


@Composable
fun MainContent(windowState: WindowState, /*googleSheetsManager: GoogleSheetsManager, prospectList: MutableList<ProspectData>*/) {
    var pastedInput by remember {mutableStateOf("")}
    var statusMessage by remember {mutableStateOf("En attente de connexion...")}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var isLoading by remember {mutableStateOf(false)}
    val logger = LoggerFactory.getLogger("App")

    val linkedInManager = LinkedInManager()

    currentProfile = linkedInManager.extractProfileData(pastedInput)

//    var spreadsheetId by remember {mutableStateOf("")}
//    var newProspect by remember {mutableStateOf(ProspectData())}

    MaterialTheme(colors = darkColors()) {
        Row(Modifier.fillMaxSize().background(Color.DarkGray).padding(10.dp)) {
            Column(Modifier.weight(2f).fillMaxHeight(), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = pastedInput,
                    onValueChange = {pastedInput = it},
                    label = {Text("Coller le texte de la page LinkedIn ici...")},
                    modifier = Modifier.fillMaxSize(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(0.12f),
                        disabledTextColor = MaterialTheme.colors.onSurface.copy(0.6f),
                        disabledBorderColor = MaterialTheme.colors.onSurface.copy(0.12f),
                        disabledLabelColor = MaterialTheme.colors.onSurface.copy(0.4f)
                    )
                )

            }
            Spacer(Modifier.width(10.dp))
            // Colonne de droite
            Column(Modifier.weight(1f).fillMaxSize()) {
                // Fiche contact
                if (isLoading) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                else currentProfile?.let {ProspectCard(it)} ?: EmptyProspectCard()
                Spacer(Modifier.width(10.dp))
                // Statut
                Text(
                    statusMessage, Modifier.padding(8.dp), color = when {
                        statusMessage.startsWith("✅") -> Color.Green
                        statusMessage.startsWith("❌") -> Color.Red
                        else -> Color.White
                    }
                )
            }
        }
    }
}