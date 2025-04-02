package utils

import androidx.compose.material.ButtonDefaults
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import java.awt.Robot
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyEvent

data class ConsoleMessage(val message: String, val type: ConsoleMessageType)
enum class ConsoleMessageType {INFO, SUCCESS, ERROR, WARNING}
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

suspend fun modalDetectionStep(condition: () -> Boolean, robot: Robot, clipboard: Clipboard, errorActionType: String, onNewClipBoardContent: (String) -> Unit, onNewMessage: (ConsoleMessage) -> Unit): Boolean {
    val maxAttempts = 100
    var attempts = 0
    while (!condition() && attempts < maxAttempts) {
        delay(250)
        copyUrlContent(robot)
        val newClipboardContent = getClipboardContent(clipboard)
        onNewClipBoardContent(newClipboardContent)
        attempts++
        onNewMessage(ConsoleMessage("⏳ Détection de la page en cours... [Tentative $attempts/$maxAttempts]", ConsoleMessageType.INFO))
    }
    if (attempts >= maxAttempts) {onNewMessage(ConsoleMessage("❌ $errorActionType après $maxAttempts tentatives", ConsoleMessageType.ERROR)); return false}
    return true
}

fun getClipboardContent(clipboard: Clipboard): String {
    return try {clipboard.getData(DataFlavor.stringFlavor) as? String ?: ""}
    catch (e: Exception) {""}
}

suspend fun copyUrlContent(robot: Robot) {
    robot.ctrlAnd(KeyEvent.VK_A)
    delay(250)
    robot.ctrlAnd(KeyEvent.VK_C)
    delay(250)
}

fun Robot.ctrlAnd(key: Int) {
    keyPress(KeyEvent.VK_CONTROL)
    keyPress(key)
    keyRelease(key)
    keyRelease(KeyEvent.VK_CONTROL)
}

@Composable
fun getTextFieldColors(colorSecondary: Color) = TextFieldDefaults.outlinedTextFieldColors(
    textColor = colorSecondary,
    focusedBorderColor = colorSecondary.copy(0.25f),
    unfocusedBorderColor = colorSecondary.copy(0.15f),
    focusedLabelColor = colorSecondary.copy(0.5f),
    unfocusedLabelColor = colorSecondary.copy(0.5f),
    placeholderColor = colorSecondary.copy(0.25f)
)

@Composable
fun getButtonColors(backgroundColor: Color, disabledBackgroundColor: Color, contentColor: Color) = ButtonDefaults.buttonColors(backgroundColor, contentColor, disabledBackgroundColor, contentColor.copy(0.5f))