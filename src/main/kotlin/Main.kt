import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.serialization.Serializable

@Serializable
data class ProspectData(val linkedinURL: String, val name: String = "", val email: String = "", val status: String = "pending")

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}