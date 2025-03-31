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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Card
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.rememberDrawerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.composable.App
import ui.composable.DrawerContent
import ui.composable.DrawerMenuItem
import ui.composable.SpacedDivider
import utils.Colors
import utils.ConsoleMessage
import utils.ConsoleMessageType
import kotlin.system.exitProcess

fun main() = application {
    val windowState = rememberWindowState(WindowPlacement.Floating, isMinimized = false, WindowPosition.PlatformDefault, DpSize(1280.dp, 720.dp))
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val applicationScope: CoroutineScope = rememberCoroutineScope()
    var isDarkTheme = remember {mutableStateOf(true)}
    val themeColors = Colors().get(isDarkTheme)

    var pastedAPI by remember {mutableStateOf("")}
    var apiKey by remember {mutableStateOf<String?>((""))}
    var isApolloValidationLoading by remember {mutableStateOf(false)}
    var statusMessage by remember {mutableStateOf(ConsoleMessage("", ConsoleMessageType.INFO))}

    val (darkGray, middleGray, lightGray) = themeColors
    var isMaximized by remember {mutableStateOf(false)}

    Window({exitApplication()}, windowState, visible = true, "LinkedIn Parser", undecorated = true) {
        var isExpandedMenuItem by remember {mutableStateOf<String?>(null)}
        val drawerWidth = if (isExpandedMenuItem != null) 0.8f else 0.2f

        ModalDrawer(
            drawerContent = {
                // Volet avant
                Card(Modifier.fillMaxHeight().fillMaxWidth(0.2f), RoundedCornerShape(0.dp, 25.dp, 25.dp, 0.dp), backgroundColor = middleGray, border = BorderStroke(1.dp, darkGray), elevation = 5.dp) {
                    Column(Modifier.fillMaxSize()) {
                        // Titre du menu
                        Column(Modifier.fillMaxWidth().padding(20.dp, 10.dp)) {
                            Text("Menu", fontSize = 24.sp, color = lightGray)
                            SpacedDivider(Modifier.fillMaxWidth().background(darkGray.copy(0.5f)), "vertical", 1.dp, 10.dp, 10.dp)
                        }

                        // Éléments du menu
                        DrawerMenuItem("Général", Icons.Filled.Settings, themeColors, isExpandedMenuItem == "Général", {isExpandedMenuItem = if (isExpandedMenuItem == "Général") null else "Général"}) {}
                        DrawerMenuItem("Customisation", Icons.Filled.Palette, themeColors, isExpandedMenuItem == "Customisation", {isExpandedMenuItem = if (isExpandedMenuItem == "Customisation") null else "Customisation"}) {}
                        DrawerMenuItem("Aide", Icons.Filled.Help, themeColors, isExpandedMenuItem == "Aide", {isExpandedMenuItem = if (isExpandedMenuItem == "Aide") null else "Aide"}) {}
                        DrawerMenuItem("Contact", Icons.Filled.Email, themeColors, isExpandedMenuItem == "Contact", {isExpandedMenuItem = if (isExpandedMenuItem == "Contact") null else "Contact"}) {}
                    }
                }

                // Volet arrière
                if (isExpandedMenuItem != null) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(drawerWidth).padding(start = 20.dp)) {
                        when (isExpandedMenuItem) {
                            "Général" -> DrawerContent(
                                themeColors, pastedAPI, apiKey.toString(), isApolloValidationLoading,
                                onApiKeyChange = {pastedAPI = it},
                                onProcessApiKey = {
                                    applicationScope.launch {
                                        isApolloValidationLoading = true
                                        apiKey = pastedAPI
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
                            )
                            "Customisation" -> Column(Modifier.fillMaxWidth().padding(20.dp)) {
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
                            "Aide" -> Column(Modifier.fillMaxWidth().padding(20.dp)) {
                                Text("Documentation", color = lightGray, fontSize = 18.sp)
                                Spacer(Modifier.height(10.dp))
                                Text("Pour utiliser cette application, copiez le contenu d'une page LinkedIn et collez-le dans la zone de texte à gauche.", color = lightGray)
                            }
                            "Contact" -> Column(Modifier.fillMaxWidth().padding(20.dp)) {
                                Text("Nous contacter", color = lightGray, fontSize = 18.sp)
                                Spacer(Modifier.height(10.dp))
                                Text("Email : pmbussy66@gmail.com", color = lightGray)
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxHeight(),
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerShape = RoundedCornerShape(0.dp, 25.dp, 25.dp, 0.dp),
            drawerElevation = 5.dp,
            drawerBackgroundColor = darkGray
        ) {
            Column(Modifier.fillMaxSize().background(darkGray)) {
                // Barre de titre
                WindowDraggableArea(Modifier.fillMaxWidth().height(45.dp).background(darkGray)) {
                    Row(Modifier.fillMaxSize().padding(15.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Titre
                        Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            // Icone de menu
                            IconButton({applicationScope.launch {if (drawerState.isOpen) drawerState.close() else drawerState.open()}}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Menu, "Menu", tint = lightGray)}
                            Spacer(Modifier.width(15.dp))
                            // Texte
                            Text("LinkedIn Parser", Modifier.height(15.dp), color = lightGray)
                        }
                        // Boutons
                        Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            // Minimiser
                            IconButton({windowState.isMinimized = true}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.KeyboardArrowDown, "Minimiser", tint = lightGray)}
                            Spacer(Modifier.width(15.dp))
                            // Maximiser / Restaurer
                            IconButton({isMaximized = !isMaximized; windowState.placement = if (isMaximized) WindowPlacement.Maximized else WindowPlacement.Floating}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Window, "Maximiser / Restaurer", tint = lightGray)}
                            Spacer(Modifier.width(15.dp))
                            // Quitter
                            IconButton({exitProcess(0)}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Close, "Quitter", tint = lightGray)}
                        }
                    }
                }
                // Contenu principal
                Box(Modifier.fillMaxSize()) {
                    App(applicationScope, themeColors, apiKey)
                }
            }
        }
    }
}