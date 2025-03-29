import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.rememberDrawerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.composable.DrawerContent
import ui.composable.WindowContent

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


    ModalDrawer(
        drawerContent = {
            DrawerContent(
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
            )
        }, Modifier, drawerState
    ) {
        WindowContent(windowState, applicationScope, drawerState, themeColors, apiKey)
    }
}