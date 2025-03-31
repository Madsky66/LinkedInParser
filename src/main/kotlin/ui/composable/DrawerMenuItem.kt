package ui.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

@Composable
fun DrawerMenuItem(title: String, icon: ImageVector, themeColors: List<Color>, isExpanded: Boolean, onClick: () -> Unit) {
    val (darkGray, middleGray, lightGray) = themeColors
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)

    Row(Modifier.fillMaxWidth().background(if (isExpanded) {darkGray} else {middleGray}).clickable(onClick = onClick).padding(20.dp, 10.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        // Icône et titre
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, title, tint = lightGray)
            Spacer(Modifier.width(10.dp))
            Text(title, color = lightGray, fontSize = 16.sp)
        }
        // Flèche d'expansion
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Expand", Modifier.rotate(rotation), lightGray)
    }
}