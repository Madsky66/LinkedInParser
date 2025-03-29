package ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomProgressIndicator(themeColors: List<Color>) {
    val  (darkGray, middleGRay, lightGray) = themeColors
    Column(Modifier.fillMaxWidth()) {
        CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally), lightGray, strokeWidth = 5.dp)
    }
}