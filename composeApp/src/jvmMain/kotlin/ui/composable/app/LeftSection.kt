package ui.composable.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import config.GlobalInstance.config as gC
import kotlinx.coroutines.CoroutineScope
import ui.composable.effect.CustomOutlinedTextFieldColors
import ui.composable.element.*
import utils.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RowScope.LeftSection(applicationScope: CoroutineScope) {
    val darkGray = gC.darkGray.value
    val middleGray = gC.middleGray.value
    val lightGray = gC.lightGray.value
    var isXLSXImportButtonEnabled by remember {mutableStateOf(true)}

    Column(Modifier.weight(0.5f).fillMaxHeight().padding(5.dp, 5.dp, 0.dp, 0.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
        // Fiche contact
        Column(Modifier.fillMaxWidth(), Arrangement.Top, Alignment.CenterHorizontally) {ProspectCard()}
        // Diviseur espacé
        SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(gC.darkGray.value.copy(0.05f)), "horizontal", 1.dp, 15.dp, 15.dp)
        // Options
        Column(Modifier.fillMaxWidth(), Arrangement.Bottom, Alignment.CenterHorizontally) {
            // Boutons
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                // Format XLSX
                Row(Modifier.clip(RoundedCornerShape(100)).background(darkGray).padding(5.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    isXLSXImportButtonEnabled = !gC.isExtractionLoading.value && !gC.isImportationLoading.value && !gC.isExportationLoading.value
                    Box(Modifier.size(50.dp).clip(RoundedCornerShape(100)).background(gC.darkGray.value).padding(10.dp), Alignment.Center) {Icon(Icons.Filled.FileCopy, "", tint = gC.lightGray.value)}
                    // Spacer
                    Spacer(Modifier.height(10.dp))
                    // Boutons
                    Row(Modifier, Arrangement.End, Alignment.CenterVertically) {
                        // Bouton d'importation
                        Card(
                            onClick = {gC.showImportModal.value = true; gC.consoleMessage.value = ConsoleMessage("⏳ En attente de sélection d'un fichier", ConsoleMessageType.INFO)},
                            modifier = Modifier,
                            enabled = isXLSXImportButtonEnabled,
                            shape = RoundedCornerShape(100),
                            backgroundColor = if (isXLSXImportButtonEnabled) {middleGray} else {darkGray},
                            contentColor = lightGray,
                            border = BorderStroke(1.dp, darkGray),
                            elevation = 10.dp
                        ) {
                            Icon(Icons.Filled.SaveAlt, "", Modifier.size(50.dp).padding(15.dp), tint = lightGray)
                        }
                        Spacer(Modifier.width(5.dp))
                        // Bouton d'exportation
                        Card(
                            onClick = {gC.showExportModal.value = true; gC.consoleMessage.value = ConsoleMessage("⏳ En attente de sélection d'un emplacement", ConsoleMessageType.INFO)},
                            modifier = Modifier,
                            enabled = gC.currentProfile.value != null,
                            shape = RoundedCornerShape(100),
                            backgroundColor = if (isXLSXImportButtonEnabled) {middleGray} else {darkGray},
                            contentColor = lightGray,
                            border = BorderStroke(1.dp, darkGray),
                            elevation = 10.dp
                        ) {
                            Icon(Icons.Filled.IosShare, "", Modifier.size(50.dp).padding(15.dp), tint = lightGray)
                        }
                        Spacer(Modifier.width(5.dp))
                    }
                }
                // Séparateur espacé
//                SpacedDivider(Modifier, "vertical", 1.dp, 10.dp,  10.dp)
//                SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(gC.darkGray.value.copy(0.05f)), "horizontal", 1.dp, 15.dp, 10.dp)

                // Google Sheets
                Row(Modifier.clip(RoundedCornerShape(100)).background(darkGray).padding(5.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    isXLSXImportButtonEnabled = !gC.isExtractionLoading.value && !gC.isImportationLoading.value && !gC.isExportationLoading.value
                    Box(Modifier.size(50.dp).clip(RoundedCornerShape(100)).background(gC.darkGray.value).padding(10.dp), Alignment.Center) {Icon(Icons.Filled.AddToDrive, "", tint = gC.lightGray.value)}
                    // Spacer
                    Spacer(Modifier.height(10.dp))
                    // Boutons
                    Row(Modifier, Arrangement.End, Alignment.CenterVertically) {
                        // Bouton d'importation
                        Card(
                            onClick = {gC.showImportModal.value = true; gC.consoleMessage.value = ConsoleMessage("⏳ En attente de sélection d'un fichier", ConsoleMessageType.INFO)},
                            modifier = Modifier,
                            enabled = !gC.isExtractionLoading.value && !gC.isImportationLoading.value && !gC.isExportationLoading.value,
                            shape = RoundedCornerShape(100),
                            backgroundColor = if (isXLSXImportButtonEnabled) {middleGray} else {darkGray},
                            contentColor = lightGray,
                            border = BorderStroke(1.dp, darkGray),
                            elevation = 10.dp
                        ) {
                            Icon(Icons.Filled.SaveAlt, "", Modifier.size(50.dp).padding(15.dp), tint = lightGray)
                        }
                        Spacer(Modifier.width(5.dp))
                        // Bouton d'exportation
                        Card(
                            onClick = {gC.showExportModal.value = true; gC.consoleMessage.value = ConsoleMessage("⏳ En attente de sélection d'un emplacement", ConsoleMessageType.INFO)},
                            modifier = Modifier,
                            enabled = gC.currentProfile.value != null,
                            shape = RoundedCornerShape(100),
                            backgroundColor = if (isXLSXImportButtonEnabled) {middleGray} else {darkGray},
                            contentColor = lightGray,
                            border = BorderStroke(1.dp, darkGray),
                            elevation = 10.dp
                        ) {
                            Icon(Icons.Filled.IosShare, "", Modifier.size(50.dp).padding(15.dp), tint = lightGray)
                        }
                        Spacer(Modifier.width(5.dp))
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