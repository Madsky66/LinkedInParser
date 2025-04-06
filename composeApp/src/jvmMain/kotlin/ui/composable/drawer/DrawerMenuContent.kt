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

fun onMenuItemTap(selectedTab: String) {
    gC.isExpandedMenuItem.value =
        when (gC.isExpandedMenuItem.value) {
            "Général" -> if (selectedTab == "Général") {""} else selectedTab.toString()
            "Customisation" -> if (selectedTab == "Customisation") {""} else selectedTab.toString()
            "Aide" -> if (selectedTab == "Aide") {""} else selectedTab.toString()
            "Contact" -> if (selectedTab == "Contact") {""} else selectedTab.toString()
            else -> selectedTab.toString()
        }
}

@Composable
fun DrawerMenuContent(applicationScope: CoroutineScope) {
    var isApolloValidationLoading by remember {mutableStateOf(false)}
    var statusMessage by remember {mutableStateOf(ConsoleMessage("", ConsoleMessageType.INFO))}
    var pastedApiKey by remember {mutableStateOf("")}

    // Volet arrière
    Row(Modifier.fillMaxHeight().fillMaxWidth(if (gC.isExpandedMenuItem.value != "") {1f} else {0.2f}).background(gC.middleGray.value)) {
        // Volet avant
        Card(Modifier.fillMaxHeight().fillMaxWidth(if (gC.isExpandedMenuItem.value != "") {0.2f} else {1f}), RoundedCornerShape(0.dp, 25.dp, 25.dp, 0.dp), backgroundColor = gC.darkGray.value, border = BorderStroke(1.dp, gC.darkGray.value), elevation = 5.dp) {
            Column(Modifier.fillMaxSize()) {
                // Titre du menu
                Column(Modifier.fillMaxWidth().padding(20.dp, 10.dp)) {
                    Text("Menu", fontSize = 24.sp, color = gC.lightGray.value)
                    SpacedDivider(Modifier.fillMaxWidth().background(gC.middleGray.value.copy(0.5f)), "vertical", 1.dp, 10.dp, 10.dp)
                }
                // Éléments du menu
                DrawerMenuItem("Général", Icons.Filled.Settings, gC.isExpandedMenuItem.value == "Général") {onMenuItemTap("Général")}
                DrawerMenuItem("Customisation", Icons.Filled.Palette, gC.isExpandedMenuItem.value == "Customisation") {onMenuItemTap("Customisation")}
                DrawerMenuItem("Aide", Icons.AutoMirrored.Filled.Help, gC.isExpandedMenuItem.value == "Aide") {onMenuItemTap("Aide")}
                DrawerMenuItem("Contact", Icons.Filled.Email, gC.isExpandedMenuItem.value == "Contact") {onMenuItemTap("Contact")}
            }
        }

        Column(Modifier.fillMaxWidth(if (gC.isExpandedMenuItem.value != "") {1f} else {0f}).padding(20.dp)) {
            when (gC.isExpandedMenuItem.value) {
                "Général" ->
                    GeneralTab(pastedApiKey) {
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
                "Customisation" -> CustomizationTab()
                "Aide" -> HelpTab()
                "Contact" -> ContactTab()
                else -> Column(Modifier.fillMaxWidth()) {}
            }
        }
    }
}

@Composable
fun GeneralTab(pastedApiKey: String, onProcessApiKey: @Composable (() -> Unit)) {
    DrawerSubMenuContent(pastedApiKey.toString(), onProcessApiKey = {onProcessApiKey})
}

@Composable
fun CustomizationTab() {
    Text("Options de thème", color = gC.lightGray.value, fontSize = 18.sp)
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text("Thème sombre [Expérimental...]", color = gC.lightGray.value)
        Switch(
            checked = gC.isDarkTheme.value,
            onCheckedChange = {gC.isDarkTheme.value = it},
            colors = SwitchDefaults.colors(
                checkedThumbColor = gC.lightGray.value,
                uncheckedThumbColor = gC.darkGray.value,
                checkedTrackColor = gC.lightGray.value.copy(0.5f),
                uncheckedTrackColor = gC.darkGray.value.copy(0.5f)
            )
        )
    }
}

@Composable
fun HelpTab() {
    Text("Documentation", color = gC.lightGray.value, fontSize = 18.sp)
    Spacer(Modifier.height(10.dp))
    Text("Pour utiliser cette application, copiez le contenu d'une page LinkedIn et collez-le dans la zone de texte à gauche.", color = gC.lightGray.value)
}

@Composable
fun ContactTab() {
    Text("Nous contacter", color = gC.lightGray.value, fontSize = 18.sp)
    Spacer(Modifier.height(10.dp))
    Text("Email : pmbussy66@gmail.com", color = gC.lightGray.value)
}