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
import data.ProspectData
import ui.composable.ProspectCard
import ui.composable.SpacedDivider
import utils.ConsoleMessage
import utils.ConsoleMessageType
import utils.getButtonColors
import utils.getTextFieldColors

@Composable
fun RowScope.ProfileAndOptionsSection(currentProfile: ProspectData?, isExtractionLoading: Boolean, isImportationLoading: Boolean, isExportationLoading: Boolean, importedFilePath: String, importedFileName: String, importedFileFormat: String, pastedURL: String, consoleMessage: ConsoleMessage, themeColors: List<Color>, onUrlChange: (String) -> Unit, onImportButtonClick: () -> Unit, onExportButtonClick: () -> Unit, onOpenUrl: (String) -> Unit) {
    var (darkGray, middleGray, lightGray) = themeColors

    // Colonne de droite
    Column(Modifier.weight(1f).fillMaxHeight().padding(5.dp, 5.dp, 0.dp, 0.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
        // Fiche contact
        Column(Modifier.fillMaxWidth(), Arrangement.Top, Alignment.CenterHorizontally) {ProspectCard(currentProfile, themeColors, isImportationLoading, isExtractionLoading)}

        // Diviseur espacé
        SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(darkGray.copy(0.05f)), "horizontal", 1.dp, 15.dp, 15.dp)

        // Options
        Column(Modifier.fillMaxWidth(), Arrangement.Bottom, Alignment.CenterHorizontally) {
            val isFileImported = (importedFileName != "" && importedFilePath != "")
            val displayFileName = if (isFileImported) {"$importedFileName.$importedFileFormat"} else {"Aucun fichier chargé"}
            val label = "Fichier chargé : "

            // Afficheur de nom de fichier
            Row(Modifier.border(BorderStroke(1.dp, darkGray)).padding(20.dp, 10.dp).fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(label, Modifier, lightGray)
                Text(displayFileName, Modifier, color = if (isFileImported) {Color.Green.copy(0.5f)} else {lightGray})
            }

            // Spacer
            Spacer(Modifier.height(10.dp))

            // Bouton d'importation de fichier
            Button(
                onClick = onImportButtonClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExtractionLoading && !isImportationLoading && !isExportationLoading,
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(100),
                colors = getButtonColors(middleGray, darkGray, lightGray)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                    Icon(Icons.Filled.SaveAlt, "")
                    Spacer(Modifier.width(10.dp))
                    Text("Importer [CSV / XLSX]")
                }
            }

            Button(
                onClick = onExportButtonClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = currentProfile != null && consoleMessage.type == ConsoleMessageType.SUCCESS,
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(100),
                colors = getButtonColors(middleGray, darkGray, lightGray)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                    Icon(Icons.Filled.IosShare, "")
                    Spacer(Modifier.width(10.dp))
                    Text("Exporter [CSV / XLSX]")
                }
            }
        }

        // Diviseur espacé
        SpacedDivider(Modifier.fillMaxWidth().padding(50.dp, 0.dp).background(darkGray.copy(0.05f)), "horizontal", 1.dp, 15.dp, 10.dp)

        // Profil LinkedIn
        Column(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterHorizontally) {
            // Saisie de l'URL
            OutlinedTextField(
                value = pastedURL,
                onValueChange = onUrlChange,
                label = {Text("Coller l'URL de la page LinkedIn ici...")},
                modifier = Modifier.fillMaxWidth().clip(RectangleShape),
                colors = getTextFieldColors(lightGray)
            )

            // Bouton de validation
            Button(
                onClick = {onOpenUrl(pastedURL)},
                modifier = Modifier.fillMaxWidth(),
                enabled = pastedURL.matches(Regex("https?://(www\\.)?linkedin\\.com/in/.*")),
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(0, 0, 50, 50),
                colors = getButtonColors(middleGray, darkGray, lightGray)
            ) {
                Text("Ouvrir le profil")
            }
        }
    }
}