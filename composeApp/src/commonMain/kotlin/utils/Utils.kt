package utils

import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color

data class ConsoleMessage(val message: String, val type: ConsoleMessageType)
enum class ConsoleMessageType {INFO, SUCCESS, ERROR, WARNING}

class Colors {
    private val darkThemeColors = listOf(
        Color(0xFF2A2A2A),  // ---------------> darkGray
        Color.DarkGray,     // ---------------> middleGray
        Color.LightGray     // ---------------> lightGray
    )
    private val lightThemeColors = listOf(
        Color.DarkGray,     // ---------------> darkGray
        Color.LightGray,    // ---------------> middleGray
        Color(0xFF0288D1)   // ---------------> lightGray
    )
    fun get(isDarkTheme: MutableState<Boolean>): List<Color> {return if (isDarkTheme.value) darkThemeColors else lightThemeColors}
}

@Composable
fun getButtonColors(backgroundColor: Color, disabledBackgroundColor: Color, contentColor: Color) = ButtonDefaults.buttonColors(backgroundColor, contentColor, disabledBackgroundColor, contentColor.copy(0.5f))