package ui.composable

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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.ConsoleMessage
import utils.ConsoleMessageType
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3

@Composable
fun DrawerMenuContent(applicationScope: CoroutineScope, themeColors: List<Color>, isDarkTheme: MutableState<Boolean>, drawerWidth: Float, isExpandedMenuItem: String, apiKey: String?, onApiKeyModified: (String) -> Unit, onMenuItemTap: (String?) -> Unit) {
    val (darkGray, middleGray, lightGray) = themeColors
    var isApolloValidationLoading by remember {mutableStateOf(false)}
    var statusMessage by remember {mutableStateOf(ConsoleMessage("", ConsoleMessageType.INFO))}
    var pastedApiKey by remember {mutableStateOf("")}

    // Volet avant
    Card(Modifier.fillMaxHeight().fillMaxWidth(0.2f), RoundedCornerShape(0.dp, 25.dp, 25.dp, 0.dp), backgroundColor = middleGray, border = BorderStroke(1.dp, darkGray), elevation = 5.dp) {
        Column(Modifier.fillMaxSize()) {
            // Titre du menu
            Column(Modifier.fillMaxWidth().padding(20.dp, 10.dp)) {
                Text("Menu", fontSize = 24.sp, color = lightGray)
                SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 10.dp, 10.dp)
            }
            // Éléments du menu
            DrawerMenuItem("Général", Icons.Filled.Settings, themeColors, isExpandedMenuItem == "Général") {onMenuItemTap("Général")}
            DrawerMenuItem("Customisation", Icons.Filled.Palette, themeColors, isExpandedMenuItem == "Customisation") {onMenuItemTap("Customisation")}
            DrawerMenuItem("Aide", Icons.Filled.Help, themeColors, isExpandedMenuItem == "Aide") {onMenuItemTap("Aide")}
            DrawerMenuItem("Contact", Icons.Filled.Email, themeColors, isExpandedMenuItem == "Contact") {onMenuItemTap("Contact")}
        }
    }

    // Volet arrière
    Box(Modifier.fillMaxHeight().fillMaxWidth(drawerWidth).padding(start = 20.dp)) {
        when (isExpandedMenuItem) {
            "Général" ->
                GeneralTab(themeColors, apiKey, pastedApiKey, isApolloValidationLoading, {pastedApiKey = it}) {
                    applicationScope.launch {
                        isApolloValidationLoading = true
                        onApiKeyModified(pastedApiKey)
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
            "Customisation" -> CustomizationTab(themeColors, isDarkTheme)
            "Aide" -> HelpTab(themeColors)
            "Contact" -> ContactTab(themeColors)
            else -> Column {}
        }
    }
}

@Composable
fun GeneralTab(themeColors: List<Color>, apiKey: String?, pastedApiKey: String?, isApolloValidationLoading: Boolean, onApiKeyModified: (String) -> Unit, onProcessApiKey: @Composable (() -> Unit)) {
    DrawerSubMenuContent(themeColors, pastedApiKey.toString(), apiKey.toString(), isApolloValidationLoading, onApiKeyModified = {onApiKeyModified}, onProcessApiKey = {onProcessApiKey})
}

@Composable
fun CustomizationTab(themeColors: List<Color>, isDarkTheme: MutableState<Boolean>) {
    val (darkGray, middleGray, lightGray) = themeColors
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Options de thème", color = lightGray, fontSize = 18.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Thème sombre [Expérimental...]", color = lightGray)
            Switch(
                checked = isDarkTheme.value,
                onCheckedChange = {isDarkTheme.value = it},
                colors = SwitchDefaults.colors(
                    checkedThumbColor = lightGray,
                    uncheckedThumbColor = darkGray,
                    checkedTrackColor = lightGray.copy(0.5f),
                    uncheckedTrackColor = darkGray.copy(0.5f)
                )
            )
        }
    }
}

@Composable
fun HelpTab(themeColors: List<Color>) {
    val (darkGray, middleGray, lightGray) = themeColors
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Documentation", color = lightGray, fontSize = 18.sp)
        Spacer(Modifier.height(10.dp))
        Text("Pour utiliser cette application, copiez le contenu d'une page LinkedIn et collez-le dans la zone de texte à gauche.", color = lightGray)
    }
}

@Composable
fun ContactTab(themeColors: List<Color>) {
    val (darkGray, middleGray, lightGray) = themeColors
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Nous contacter", color = lightGray, fontSize = 18.sp)
        Spacer(Modifier.height(10.dp))
        Text("Email : pmbussy66@gmail.com", color = lightGray)
    }
}