import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalDrawer
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.rememberDrawerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.composable.App
import ui.composable.getButtonColors
import ui.composable.getTextFieldColors
import kotlin.system.exitProcess

data class StatusMessage(val message: String, val type: StatusType)
enum class StatusType {INFO, SUCCESS, ERROR, WARNING}
enum class ExportFormat {XLSX, CSV}
class Colors {
    fun get(isDarkTheme: MutableState<Boolean>): List<Color> {
        var themeColors =
            if (isDarkTheme.value) {listOf(
                Color(0xFF2A2A2A),  //---------------> darkGray
                Color.DarkGray,     //---------------> middleGray
                Color.LightGray     //---------------> lightGray
            )}
            else {listOf(
                Color.DarkGray,     //---------------> darkGray
                Color.LightGray,    //---------------> middleGray
                Color.LightGray     //---------------> darkGray
            )}
        return themeColors
    }
}

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Floating, isMinimized = false, position = WindowPosition.PlatformDefault, size = DpSize(1280.dp, 720.dp))
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val applicationScope: CoroutineScope = rememberCoroutineScope()
    var isDarkTheme = remember {mutableStateOf(true)}
    val themeColors = Colors().get(isDarkTheme)

    var pastedAPI by remember {mutableStateOf("")}
    var apiKey by remember {mutableStateOf<String?>((""))}
    var isApolloValidationLoading by remember {mutableStateOf(false)}
    var statusMessage by remember {mutableStateOf(StatusMessage("", StatusType.INFO))}


    ModalDrawer(drawerContent = {DrawerContent(
        themeColors, pastedAPI, isApolloValidationLoading,
        onApiKeyChange = {pastedAPI  = it},
        onProcessApiKey = {
            applicationScope.launch {
                isApolloValidationLoading = true
                apiKey = pastedAPI
                statusMessage = StatusMessage("⏳ Validation de la clé API par Apollo en cours...", StatusType.INFO)
                try {
                    // <--- Vérifier la validité de la clé ici
                    delay(500) // Simulation de la validation
                    statusMessage = StatusMessage("✅ La clé API a bien été validée par Apollo", StatusType.SUCCESS)
                }
                catch (e: Exception) {statusMessage = StatusMessage("❌ Erreur lors de la validation de la clé API par Apollo : ${e.message}", StatusType.ERROR)}
                isApolloValidationLoading = false
            }
        }
    )}, Modifier, drawerState) {
        WindowContent(windowState, applicationScope, drawerState, themeColors, apiKey)
    }
}

@Composable
fun DrawerContent(themeColors: List<Color>, pastedAPI: String, isApolloValidationLoading: Boolean, onApiKeyChange: (String) -> Unit, onProcessApiKey: (String) -> Unit) {
    val (darkGray, middleGray, lightGray) = themeColors

    Box(Modifier.fillMaxHeight().fillMaxWidth(0.25f).background(darkGray)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            // Zone de texte
            OutlinedTextField(
                value = pastedAPI,
                onValueChange = onApiKeyChange,
                modifier = Modifier.clip(RectangleShape).weight(2f),
                textStyle = TextStyle.Default,
                label = {Text("Clé API Apollo...")},
                colors = getTextFieldColors(lightGray)
            )

            // Spacer
            Spacer(Modifier.width(10.dp))

            // Bouton de validation
            Button(
                onClick = {onProcessApiKey(pastedAPI)},
                modifier = Modifier.padding(top = 8.dp).weight(0.75f).height(54.dp),
                enabled = pastedAPI.isNotBlank(),
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(0, 100, 100, 0),
                colors = getButtonColors(middleGray, darkGray, lightGray)
            ) {
                if (!isApolloValidationLoading) {Icon(Icons.Filled.Send, "")} else {CircularProgressIndicator(Modifier.align(Alignment.CenterVertically))}
            }
        }
    }
}

@Composable
fun WindowContent(windowState: WindowState, applicationScope: CoroutineScope, drawerState: DrawerState, themeColors: List<Color>, apiKey: String?) {
    val (darkGray, middleGray, lightGray) = themeColors
    var isMaximized by remember {mutableStateOf(false)}

    Window(onCloseRequest = {/*exitApplication()*/}, state = windowState, visible = true, title = "LinkedIn Parser", undecorated = true) {

        Column(Modifier.fillMaxSize()) {
            // Barre de titre
            WindowDraggableArea(Modifier.fillMaxWidth().height(45.dp).background(darkGray)) {
                Row(Modifier.fillMaxSize().padding(15.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    // Titre
                    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Icone de menu
                        IconButton(onClick = {applicationScope.launch {if (drawerState.isOpen) drawerState.close() else drawerState.open()}}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Menu, "Menu", tint = lightGray)}
                        Spacer(Modifier.width(15.dp))
                        // Texte
                        Text("LinkedIn Parser", Modifier.height(15.dp), color = lightGray)
                    }
                    // Boutons
                    Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        // Minimiser
                        IconButton(onClick = {windowState.isMinimized = true}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.KeyboardArrowDown, "Minimiser", tint = lightGray)}
                        Spacer(Modifier.width(15.dp))
                        // Maximiser / Restaurer
                        IconButton(onClick = {isMaximized = !isMaximized; windowState.placement = if (isMaximized) WindowPlacement.Maximized else WindowPlacement.Floating}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Window, "Maximiser / Restaurer", tint = lightGray)}
                        Spacer(Modifier.width(15.dp))
                        // Quitter
                        IconButton(onClick = {exitProcess(0)}, Modifier.size(15.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Close, "Quitter", tint = lightGray)}
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