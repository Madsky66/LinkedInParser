package ui.composable

import GoogleSheetsManager
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import data.ProspectData
import manager.LinkedInManager
import org.slf4j.LoggerFactory
import ui.theme.Colors

@Composable
fun MainContent(/*googleSheetsManager: GoogleSheetsManager, prospectList: MutableList<ProspectData>*/) {
    var pastedInput by remember {mutableStateOf("")}
    var pastedURL by remember {mutableStateOf("")}
    var statusMessage by remember {mutableStateOf("En attente de données...")}
    var currentProfile by remember {mutableStateOf<ProspectData?>(null)}
    var isLoading by remember {mutableStateOf(false)}
    val logger = LoggerFactory.getLogger("App")

    val linkedInManager = LinkedInManager()
    val googleSheetsManager = GoogleSheetsManager()

//    var spreadsheetId by remember {mutableStateOf("")}
//    var newProspect by remember {mutableStateOf(ProspectData())}

    MaterialTheme(colors = darkColors()) {
        Column(Modifier.fillMaxSize().background(Color.DarkGray).padding(10.dp)) {
            Row(Modifier.weight(0.9f).fillMaxWidth()) {
                // Zone de texte
                Column(Modifier.weight(2f).fillMaxHeight(), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = pastedInput,
                        onValueChange = {
                            pastedInput = it
                            currentProfile = linkedInManager.extractProfileData(pastedInput)
                        },
                        label = {Text("Coller le texte de la page LinkedIn ici...")},
                        modifier = Modifier.weight(0.9f).fillMaxWidth().clip(RectangleShape),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.LightGray,
                            focusedBorderColor = Color.LightGray.copy(0.25f),
                            unfocusedBorderColor = Color.LightGray.copy(0.15f),
                            focusedLabelColor = Color.LightGray.copy(0.5f),
                            unfocusedLabelColor = Color.LightGray.copy(0.5f),
                            placeholderColor = Color.LightGray.copy(0.25f)
                        )
                    )
                }

                // Spacer
                Spacer(Modifier.width(10.dp))

                // Colonne de droite
                Column(Modifier.weight(1f).fillMaxHeight().padding(5.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                    Column(Modifier.fillMaxWidth()) {
                        // Fiche contact
                        if (isLoading) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                        else currentProfile?.let {ProspectCard(it)} ?: EmptyProspectCard()

                        // Spaced Divider
                        Spacer(Modifier.height(15.dp))
                        Divider(Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                    }

                    // Boutons
                    Column(Modifier.fillMaxSize(), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                        Column(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = pastedURL,
                                onValueChange = {pastedURL = it},
                                label = {Text("Coller l'URL de la page LinkedIn ici...")},
                                modifier = Modifier.fillMaxWidth().clip(RectangleShape),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = Color.LightGray,
                                    focusedBorderColor = Color.LightGray.copy(0.25f),
                                    unfocusedBorderColor = Color.LightGray.copy(0.15f),
                                    focusedLabelColor = Color.LightGray.copy(0.5f),
                                    unfocusedLabelColor = Color.LightGray.copy(0.5f),
                                    placeholderColor = Color.LightGray.copy(0.25f)
                                )
                            )
                            Button(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(),
                                enabled = pastedURL.isNotEmpty(),
                                elevation = ButtonDefaults.elevation(10.dp),
                                shape = RoundedCornerShape(0, 0, 50, 50),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.DarkGray,
                                    contentColor = Color.LightGray,
                                    disabledBackgroundColor = Colors().DARK_GRAY,
                                    disabledContentColor = Color.DarkGray,
                                )
                            ) {
                                Text("Ouvrir le profil")
                            }

                            // Spaced Divider
                            Spacer(Modifier.height(15.dp))
                            Divider(Modifier.fillMaxWidth())
                            Spacer(Modifier.height(15.dp))
                        }

                        Button(
                            onClick = {googleSheetsManager.exportToCSV(currentProfile!!, "src/main/resources/extra/data_export.csv")},
                            modifier = Modifier.fillMaxWidth(),
                            enabled = currentProfile?.fullName != "Nom inconnu" && currentProfile?.fullName != "",
                            elevation = ButtonDefaults.elevation(10.dp),
                            shape = RoundedCornerShape(100),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.DarkGray,
                                contentColor = Color.LightGray,
                                disabledBackgroundColor = Colors().DARK_GRAY,
                                disabledContentColor = Color.DarkGray,
                            )
                        ) {
                            Text("Extraire [CSV]")
                        }
                    }
                }
            }

            // Spacer
            Spacer(Modifier.height(10.dp))

            // Console
            Column(Modifier.weight(0.1f).fillMaxWidth().background(Color.Black).padding(10.dp)) {
                Text(
                    statusMessage, Modifier.padding(10.dp), color = when {
                        statusMessage.startsWith("✅") -> Color.Green
                        statusMessage.startsWith("❌") -> Color.Red
                        else -> Color.LightGray
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun MainContentPreview() {
    MainContent()
}