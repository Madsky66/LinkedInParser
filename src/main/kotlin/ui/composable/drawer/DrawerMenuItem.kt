package ui.composable.drawer

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import config.GlobalConfig

@Composable
fun DrawerMenuItem(title: String, icon: ImageVector, gC: GlobalConfig, isExpanded: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)

    Row(Modifier.fillMaxWidth().background(if (isExpanded) {gC.darkGray.value} else {gC.middleGray.value}).clickable(onClick = onClick).padding(20.dp, 10.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        // Icône et titre
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, title, tint = gC.lightGray.value)
            Spacer(Modifier.width(10.dp))
            Text(title, color = gC.lightGray.value, fontSize = 16.sp)
        }
        // Flèche d'expansion
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Expand", Modifier.rotate(rotation), gC.lightGray.value)
    }
}