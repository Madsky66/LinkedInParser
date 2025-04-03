package ui.composable.drawer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import config.GlobalConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.composable.element.SpacedDivider
import utils.ConsoleMessage
import utils.ConsoleMessageType

fun onMenuItemTap(gC: GlobalConfig, selectedTab: String) {
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
fun DrawerMenuContent(applicationScope: CoroutineScope, gC: GlobalConfig) {
    var isApolloValidationLoading by remember {mutableStateOf(false)}
    var statusMessage by remember {mutableStateOf(ConsoleMessage("", ConsoleMessageType.INFO))}
    var pastedApiKey by remember {mutableStateOf("")}

    // Volet avant
    Card(Modifier.fillMaxHeight().fillMaxWidth(0.2f), RoundedCornerShape(0.dp, 25.dp, 25.dp, 0.dp), backgroundColor = gC.middleGray.value, border = BorderStroke(1.dp, gC.darkGray.value), elevation = 5.dp) {
        Column(Modifier.fillMaxSize()) {
            // Titre du menu
            Column(Modifier.fillMaxWidth().padding(20.dp, 10.dp)) {
                Text("Menu", fontSize = 24.sp, color = gC.lightGray.value)
                SpacedDivider(Modifier.fillMaxWidth().background(gC.darkGray.value.copy(0.5f)), "vertical", 1.dp, 10.dp, 10.dp)
            }
            // Éléments du menu
            DrawerMenuItem("Général", Icons.Filled.Settings, gC, gC.isExpandedMenuItem.value == "Général") {onMenuItemTap(gC, "Général")}
            DrawerMenuItem("Customisation", Icons.Filled.Palette, gC, gC.isExpandedMenuItem.value == "Customisation") {onMenuItemTap(gC, "Customisation")}
            DrawerMenuItem("Aide", Icons.Filled.Help, gC, gC.isExpandedMenuItem.value == "Aide") {onMenuItemTap(gC, "Aide")}
            DrawerMenuItem("Contact", Icons.Filled.Email, gC, gC.isExpandedMenuItem.value == "Contact") {onMenuItemTap(gC, "Contact")}
        }
    }

    // Volet arrière
    Box(Modifier.fillMaxHeight().fillMaxWidth(gC.drawerWidth.value).padding(start = 20.dp)) {
        when (gC.isExpandedMenuItem.value) {
            "Général" ->
                GeneralTab(gC, pastedApiKey) {
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
            "Customisation" -> CustomizationTab(gC)
            "Aide" -> HelpTab(gC)
            "Contact" -> ContactTab(gC)
            else -> Column {}
        }
    }
}

@Composable
fun GeneralTab(gC: GlobalConfig, pastedApiKey: String, onProcessApiKey: @Composable (() -> Unit)) {
    DrawerSubMenuContent(gC, pastedApiKey.toString(), onProcessApiKey = {onProcessApiKey})
}

@Composable
fun CustomizationTab(gC: GlobalConfig) {
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
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
}

@Composable
fun HelpTab(gC: GlobalConfig) {
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Documentation", color = gC.lightGray.value, fontSize = 18.sp)
        Spacer(Modifier.height(10.dp))
        Text("Pour utiliser cette application, copiez le contenu d'une page LinkedIn et collez-le dans la zone de texte à gauche.", color = gC.lightGray.value)
    }
}

@Composable
fun ContactTab(gC: GlobalConfig) {
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Nous contacter", color = gC.lightGray.value, fontSize = 18.sp)
        Spacer(Modifier.height(10.dp))
        Text("Email : pmbussy66@gmail.com", color = gC.lightGray.value)
    }
}