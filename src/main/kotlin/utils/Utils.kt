package utils

import androidx.compose.material.ButtonDefaults
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
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

suspend fun detectLinkedinProfile(robot: Robot) {
    robot.ctrlAnd(KeyEvent.VK_A)
    delay(100)
    robot.ctrlAnd(KeyEvent.VK_C)
}

suspend fun setClipboardWithCheck(value: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val text = StringSelection(value)
    repeat(5) {attempt ->
        try {
            runBlocking {clipboard.setContents(text, null)}
            delay(250)
            val pastedText = clipboard.getData(DataFlavor.stringFlavor) as String
            if (pastedText == value) {
                println("✅ Mise à jour confirmée : $pastedText")
                return
            }
            else {println("⚠️ Mismatch ! Attendu : \"$value\", Actuel : \"$pastedText\"")}
        }
        catch (e: Exception) {println("Erreur d'accès au presse-papiers : ${e.message}")}
        delay(100)
    }
    println("❌ Échec de la mise à jour du presse-papiers après plusieurs tentatives.")
}

suspend fun copyUrlContent(robot: Robot) {
    robot.ctrlAnd(KeyEvent.VK_A)
    delay(250)
    robot.ctrlAnd(KeyEvent.VK_C)
    delay(250)
    robot.ctrlAnd(KeyEvent.VK_F)
    delay(250)
    setClipboardWithCheck("Coordonnées")
    delay(250)
    robot.ctrlAnd(KeyEvent.VK_V)
    delay(250)
    robot.keyPress(KeyEvent.VK_ENTER)
    robot.keyRelease(KeyEvent.VK_ENTER)
    delay(250)
    robot.keyPress(KeyEvent.VK_ESCAPE)
    robot.keyRelease(KeyEvent.VK_ESCAPE)
    delay(250)
    robot.keyPress(KeyEvent.VK_ENTER)
    robot.keyRelease(KeyEvent.VK_ENTER)
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