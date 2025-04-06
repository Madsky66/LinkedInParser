package ui.composable.drawer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import config.GlobalInstance.config as gC
import kotlinx.coroutines.*
import ui.composable.element.SpacedDivider
import utils.*

@Composable
fun DrawerMenuContent(applicationScope: CoroutineScope) {
    var isApolloValidationLoading by remember {mutableStateOf(false)}
    var statusMessage by remember {mutableStateOf(ConsoleMessage("", ConsoleMessageType.INFO))}
    var pastedApiKey by remember {mutableStateOf("")}
    val darkGray = gC.darkGray.value
    val middleGray = gC.middleGray.value
    val lightGray = gC.lightGray.value
    val generalTab = "Général"
    val customizationTab = "Customisation"
    val helpTab = "Aide"
    val contactTab = "Contact"

    fun onMenuItemTap(selectedTab: String) {
        gC.isExpandedMenuItem.value =
            when (gC.isExpandedMenuItem.value) {
                generalTab -> if (selectedTab == generalTab) {""} else selectedTab.toString()
                customizationTab -> if (selectedTab == customizationTab) {""} else selectedTab.toString()
                helpTab -> if (selectedTab == helpTab) {""} else selectedTab.toString()
                contactTab -> if (selectedTab == contactTab) {""} else selectedTab.toString()
                else -> selectedTab.toString()
            }
    }

    Row(Modifier.fillMaxHeight().fillMaxWidth(if (gC.isExpandedMenuItem.value != "") {0.5f} else {0.2f})) {
        // Volet avant
        Card(Modifier.fillMaxHeight().fillMaxWidth(if (gC.isExpandedMenuItem.value != "") {0.4f} else {1f}), RoundedCornerShape(0.dp, 25.dp, 25.dp, 0.dp), backgroundColor = darkGray, border = BorderStroke(1.dp, darkGray), elevation = 5.dp) {
            Column(Modifier.fillMaxSize()) {
                // Titre du menu
                Column(Modifier.fillMaxWidth().padding(20.dp, 10.dp)) {
                    Text("Menu", fontSize = 24.sp, color = lightGray)
                    SpacedDivider(Modifier.fillMaxWidth().background(middleGray.copy(0.5f)), "vertical", 1.dp, 10.dp, 10.dp)
                }
                // Éléments du menu
                DrawerMenuItem(generalTab, Icons.Filled.Settings, gC.isExpandedMenuItem.value == generalTab) {onMenuItemTap(generalTab)}
                DrawerMenuItem(customizationTab, Icons.Filled.Palette, gC.isExpandedMenuItem.value == customizationTab) {onMenuItemTap(customizationTab)}
                DrawerMenuItem(helpTab, Icons.AutoMirrored.Filled.Help, gC.isExpandedMenuItem.value == helpTab) {onMenuItemTap(helpTab)}
                DrawerMenuItem(contactTab, Icons.Filled.Email, gC.isExpandedMenuItem.value == contactTab) {onMenuItemTap(contactTab)}
            }
        }
        // Volet arrière
        if (gC.isExpandedMenuItem.value != "") {
            Column(Modifier.padding(25.dp)) {
                when (gC.isExpandedMenuItem.value) {
                    generalTab ->
                        GeneralDrawerTab(pastedApiKey) {
                            applicationScope.launch {
                                isApolloValidationLoading = true
                                gC.apiKey.value = pastedApiKey
                                statusMessage = ConsoleMessage("⏳ Validation de la clé API par Apollo en cours...", ConsoleMessageType.INFO)
                                try {
                                    // <--- Vérifier la validité de la clé ici
                                    delay(500) // Simulation de la validation
                                    statusMessage = ConsoleMessage("✅ La clé API a bien été validée par Apollo", ConsoleMessageType.SUCCESS)
                                }
                                catch (e: Exception) {statusMessage = ConsoleMessage("❌ Erreur lors de la validation de la clé API par Apollo : ${e.message}", ConsoleMessageType.ERROR)}
                                isApolloValidationLoading = false
                            }
                        }
                    customizationTab -> CustomizationDrawerTab()
                    helpTab -> HelpDrawerTab()
                    contactTab -> ContactDrawerTab()
                    else -> Column(Modifier.fillMaxWidth()) {}
                }
            }
        }
    }
}