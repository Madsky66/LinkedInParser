package utils

import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color

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

@Composable
fun getButtonColors(backgroundColor: Color, disabledBackgroundColor: Color, contentColor: Color) = ButtonDefaults.buttonColors(backgroundColor, contentColor, disabledBackgroundColor, contentColor.copy(0.5f))