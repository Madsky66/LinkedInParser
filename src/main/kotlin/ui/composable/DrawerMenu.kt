package ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

data class MenuItem(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
fun DrawerMenu(menuItems: List<MenuItem>) {
    Column(Modifier.padding(16.dp)) {
        Text("Menu Principal", style = MaterialTheme.typography.h6)
        Divider()
        menuItems.forEach {item -> DrawerMenuItem(icon = item.icon, label = item.label, onClick = item.onClick)}
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {onClick()}) {
        Icon(icon, contentDescription = label)
        Spacer(Modifier.width(16.dp))
        Text(label)
    }
}