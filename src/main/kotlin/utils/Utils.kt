package utils

import androidx.compose.material.ButtonDefaults
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
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

suspend fun detectLinkedinProfile(robot: Robot) {
    // Ctrl + A
    robot.keyPress(KeyEvent.VK_CONTROL)
    robot.keyPress(KeyEvent.VK_A)
    robot.keyRelease(KeyEvent.VK_A)
    robot.keyRelease(KeyEvent.VK_CONTROL)
    delay(100)

    // Ctrl + C
    robot.keyPress(KeyEvent.VK_CONTROL)
    robot.keyPress(KeyEvent.VK_C)
    robot.keyRelease(KeyEvent.VK_C)
    robot.keyRelease(KeyEvent.VK_CONTROL)
    delay(100)
}

suspend fun openInfosModale(robot: Robot) {
    delay(100)
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val text = StringSelection("Coordonnées")
    clipboard.setContents(text, null)
    delay(100)

    // Ctrl + F
    robot.keyPress(KeyEvent.VK_CONTROL)
    robot.keyPress(KeyEvent.VK_F)
    robot.keyRelease(KeyEvent.VK_F)
    robot.keyRelease(KeyEvent.VK_CONTROL)
    delay(100)

    // Ctrl + V
    robot.keyPress(KeyEvent.VK_CONTROL)
    robot.keyPress(KeyEvent.VK_V)
    robot.keyRelease(KeyEvent.VK_V)
    robot.keyRelease(KeyEvent.VK_CONTROL)
    delay(100)

    // Escape
    robot.keyPress(KeyEvent.VK_ESCAPE)
    robot.keyRelease(KeyEvent.VK_ESCAPE)
    delay(100)

    // Enter
    robot.keyPress(KeyEvent.VK_ENTER)
    robot.keyRelease(KeyEvent.VK_ENTER)
    delay(100)
}

suspend fun copyUrlContent(robot: Robot) {
    // Ctrl + A
    robot.keyPress(KeyEvent.VK_CONTROL)
    robot.keyPress(KeyEvent.VK_A)
    robot.keyRelease(KeyEvent.VK_A)
    robot.keyRelease(KeyEvent.VK_CONTROL)
    delay(100)

    // Ctrl + C
    robot.keyPress(KeyEvent.VK_CONTROL)
    robot.keyPress(KeyEvent.VK_C)
    robot.keyRelease(KeyEvent.VK_C)
    robot.keyRelease(KeyEvent.VK_CONTROL)
    delay(100)
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
fun getButtonColors(backgroundColor: Color, disabledBackgroundColor: Color, contentColor: Color) = ButtonDefaults.buttonColors(backgroundColor, contentColor, disabledBackgroundColor, disabledContentColor = contentColor.copy(0.5f))
