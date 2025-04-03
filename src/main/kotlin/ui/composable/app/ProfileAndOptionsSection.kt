package ui.composable.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import config.GlobalConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import manager.UrlManager
import ui.composable.ProspectCard
import ui.composable.SpacedDivider
import utils.ConsoleMessage
import utils.ConsoleMessageType
import utils.getButtonColors
import utils.getTextFieldColors
import java.awt.Desktop
import java.net.URI

@Composable
fun RowScope.ProfileAndOptionsSection(applicationScope: CoroutineScope, gC: GlobalConfig, urlManager: UrlManager, importedFilePath: String, importedFileName: String, importedFileFormat: String) {

    // Colonne de droite
    Column(Modifier.weight(1f).fillMaxHeight().padding(5.dp, 5.dp, 0.dp, 0.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
        // Fiche contact
        Column(Modifier.fillMaxWidth(), Arrangement.Top, Alignment.CenterHorizontally) {ProspectCard(gC)}

        // Diviseur espacé
        SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(gC.darkGray.value.copy(0.05f)), "horizontal", 1.dp, 15.dp, 15.dp)

        // Options
        Column(Modifier.fillMaxWidth(), Arrangement.Bottom, Alignment.CenterHorizontally) {
            val isFileImported = (importedFileName != "" && importedFilePath != "")
            val displayFileName = if (isFileImported) {"$importedFileName.$importedFileFormat"} else {"Aucun fichier chargé"}
            val label = "Fichier chargé : "

            // Afficheur de nom de fichier
            Row(Modifier.border(BorderStroke(1.dp, gC.darkGray.value)).padding(20.dp, 10.dp).fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(label, Modifier, gC.lightGray.value)
                Text(displayFileName, Modifier, color = if (isFileImported) {Color.Green.copy(0.5f)} else {gC.lightGray.value})
            }

            // Spacer
            Spacer(Modifier.height(10.dp))

            // Bouton d'importation de fichier
            Button(
                onClick = {gC.showImportModal.value = true},
                modifier = Modifier.fillMaxWidth(),
                enabled = !gC.isExtractionLoading.value && !gC.isImportationLoading.value && !gC.isExportationLoading.value,
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(100),
                colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                    Icon(Icons.Filled.SaveAlt, "")
                    Spacer(Modifier.width(10.dp))
                    Text("Importer [CSV / XLSX]")
                }
            }

            Button(
                onClick = {gC.showExportModal.value = true},
                modifier = Modifier.fillMaxWidth(),
                enabled = gC.currentProfile.value != null && gC.consoleMessage.value.type == ConsoleMessageType.SUCCESS,
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(100),
                colors = getButtonColors(gC.middleGray.value, gC.darkGray.value, gC.lightGray.value)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                    Icon(Icons.Filled.IosShare, "")
                    Spacer(Modifier.width(10.dp))
                    Text("Exporter [CSV / XLSX]")
                }
            }
        }

        // Diviseur espacé
        SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(gC.darkGray.value.copy(0.05f)), "horizontal", 1.dp, 15.dp, 10.dp)

        // Profil LinkedIn
        Column(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterHorizontally) {
            // Saisie de l'URL
            OutlinedTextField(
                value = gC.pastedUrl.value,
                onValueChange = {gC.pastedUrl.value = it},
                label = {Text("Coller l'URL de la page LinkedIn ici...")},
                modifier = Modifier.fillMaxWidth().clip(RectangleShape),
                colors = getTextFieldColors(gC.lightGray.value)
            )

            // Bouton de validation
            Button(
                onClick = {
                    applicationScope.launch {
                        if (gC.pastedUrl.value.isNotBlank()) {
                            try {
                                if (!Desktop.isDesktopSupported()) {gC.consoleMessage.value = ConsoleMessage("❌ Votre système ne supporte pas Desktop browsing.", ConsoleMessageType.ERROR); return@launch}
                                val uri = URI("${gC.pastedUrl.value}/overlay/contact-info/")
                                gC.consoleMessage.value = ConsoleMessage("⏳ Ouverture de la page LinkedIn en cours...", ConsoleMessageType.INFO)
                                Desktop.getDesktop().browse(uri)
                                urlManager.openPastedUrl(applicationScope)
                            }
                            catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'ouverture de l'URL : ${e.message}", ConsoleMessageType.ERROR)}
                        }
                    }
                },
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