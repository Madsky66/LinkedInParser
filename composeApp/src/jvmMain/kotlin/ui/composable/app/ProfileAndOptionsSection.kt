package ui.composable.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import config.GlobalInstance.config as gC
import kotlinx.coroutines.CoroutineScope
import ui.composable.effect.CustomOutlinedTextFieldColors
import ui.composable.element.*
import utils.*

@Composable
fun RowScope.ProfileAndOptionsSection(applicationScope: CoroutineScope) {
    val isFileImported = (gC.fileInstance.value != null && gC.consoleMessage.value.message.contains("✅ Importation"))

    Column(Modifier.weight(1f).fillMaxHeight().padding(5.dp, 5.dp, 0.dp, 0.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
        // Fiche contact
        Column(Modifier.fillMaxWidth(), Arrangement.Top, Alignment.CenterHorizontally) {ProspectCard()}
        // Diviseur espacé
        SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(gC.darkGray.value.copy(0.05f)), "horizontal", 1.dp, 15.dp, 15.dp)
        // Options
        Column(Modifier.fillMaxWidth(), Arrangement.Bottom, Alignment.CenterHorizontally) {
            // Boutons
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                // Format XLSX
                Column(Modifier.weight(1f), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
                    Box(Modifier.size(50.dp).clip(RoundedCornerShape(100)).background(gC.darkGray.value).padding(10.dp), Alignment.Center) {Icon(Icons.Filled.FileCopy, "", tint = gC.lightGray.value)}
                    // Spacer
                    Spacer(Modifier.height(10.dp))
                    // Bouton d'importation
                    Button(
                        onClick = {gC.showImportModal.value = true; gC.consoleMessage.value = ConsoleMessage("⏳ En attente de sélection d'un fichier", ConsoleMessageType.INFO)},
                        modifier = Modifier.fillMaxWidth(0.75f),
                        enabled = !gC.isExtractionLoading.value && !gC.isImportationLoading.value && !gC.isExportationLoading.value,
                        elevation = ButtonDefaults.elevation(10.dp),
                        shape = RoundedCornerShape(100),
                        colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
                    ) {
                        Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                            Icon(Icons.Filled.SaveAlt, "")
                            Spacer(Modifier.width(10.dp))
                            Text("Importer")
                        }
                    }
                    // Bouton d'exportation
                    Button(
                        onClick = {gC.showExportModal.value = true; gC.consoleMessage.value = ConsoleMessage("⏳ En attente de sélection d'un emplacement", ConsoleMessageType.INFO)},
                        modifier = Modifier.fillMaxWidth(0.75f),
                        enabled = gC.currentProfile.value != null,
                        elevation = ButtonDefaults.elevation(10.dp),
                        shape = RoundedCornerShape(100),
                        colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
                    ) {
                        Row(Modifier, Arrangement.Center, Alignment.CenterVertically) {
                            Icon(Icons.Filled.IosShare, "")
                            Spacer(Modifier.width(10.dp))
                            Text("Exporter")
                        }
                    }
                }
                // Séparateur espacé
//                SpacedDivider(Modifier, "vertical", 1.dp, 10.dp,  10.dp)
//                SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(gC.darkGray.value.copy(0.05f)), "horizontal", 1.dp, 15.dp, 10.dp)

                // Google Sheets
                Column(Modifier.weight(1f), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
                    // Bouton d'importation
                    Button(
                        onClick = {gC.showImportModal.value = true; gC.consoleMessage.value = ConsoleMessage("⏳ En attente de sélection d'un fichier", ConsoleMessageType.INFO)},
                        modifier = Modifier.fillMaxWidth(0.75f),
                        enabled = !gC.isExtractionLoading.value && !gC.isImportationLoading.value && !gC.isExportationLoading.value,
                        elevation = ButtonDefaults.elevation(10.dp),
                        shape = RoundedCornerShape(100),
                        colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
                    ) {
                        Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                            Icon(Icons.Filled.SaveAlt, "")
                            Spacer(Modifier.width(10.dp))
                            Text("Importer")
                        }
                    }
                    // Bouton d'exportation
                    Button(
                        onClick = {gC.showExportModal.value = true; gC.consoleMessage.value = ConsoleMessage("⏳ En attente de sélection d'un emplacement", ConsoleMessageType.INFO)},
                        modifier = Modifier.fillMaxWidth(0.75f),
                        enabled = gC.currentProfile.value != null,
                        elevation = ButtonDefaults.elevation(10.dp),
                        shape = RoundedCornerShape(100),
                        colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
                    ) {
                        Row(Modifier, Arrangement.Center, Alignment.CenterVertically) {
                            Icon(Icons.Filled.IosShare, "")
                            Spacer(Modifier.width(10.dp))
                            Text("Exporter")
                        }
                    }
                }
            }
        }
        // Diviseur espacé
        SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(gC.darkGray.value.copy(0.05f)), "horizontal", 1.dp, 15.dp, 10.dp)
        // Profil LinkedIn
        Column(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterHorizontally) {
            // Saisie de l'URL
            OutlinedTextField(gC.pastedUrl.value, {gC.pastedUrl.value = it}, Modifier.fillMaxWidth().clip(RectangleShape), label = {Text("Coller l'URL de la page LinkedIn ici...")}, colors = CustomOutlinedTextFieldColors())
            // Bouton de validation
            Button(
                onClick = {gC.urlManager.openPastedUrl(applicationScope)},
                modifier = Modifier.fillMaxWidth(),
                enabled = gC.pastedUrl.value.matches(Regex("https?://(www\\.)?linkedin\\.com/in/.*")),
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(0, 0, 50, 50),
                colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
            ) {
                Text("Ouvrir le profil")
            }
        }
    }
}