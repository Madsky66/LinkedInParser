package ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Window
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import config.GlobalConfig

@Composable
fun AppTitleBar(gC: GlobalConfig, onToggleDrawer: () -> Unit, onMinimizeWindow: () -> Unit, onToggleMaximizeOrRestore: () -> Unit, onCloseApp: () -> Unit) {
    val lightGray = gC.lightGray.value

    Row(Modifier.fillMaxSize().padding(15.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        // Titre
        Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            IconButton(onToggleDrawer, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Menu, "Menu", tint = lightGray)} // Bouton de menu
            Spacer(Modifier.width(15.dp)) // Spacer
            Text("LinkedIn Parser", fontSize = 15.sp, color = lightGray) // Texte
        }
        // Boutons
        Row(Modifier.fillMaxHeight(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            IconButton(onMinimizeWindow, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.KeyboardArrowDown, "Minimiser", tint = lightGray)} // Minimiser
            Spacer(Modifier.width(15.dp)) // Spacer
            IconButton(onToggleMaximizeOrRestore, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Window, "Maximiser / Restaurer", tint = lightGray)} // Maximiser / Restaurer
            Spacer(Modifier.width(15.dp)) // Spacer
            IconButton(onCloseApp, Modifier.size(25.dp).clip(RoundedCornerShape(100))) {Icon(Icons.Filled.Close, "Quitter", tint = lightGray)} // Quitter
        }
    }
}