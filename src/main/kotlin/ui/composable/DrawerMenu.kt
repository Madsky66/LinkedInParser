package ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun DrawerMenu() {
    Column(Modifier.padding(16.dp)) {
        Text("Menu Principal", style = MaterialTheme.typography.h6)
        Divider()

        // Section Gestion
        Text("Gestion", style = MaterialTheme.typography.subtitle1)
        DrawerMenuItem(icon = Icons.Filled.Home, label = "Accueil")
        DrawerMenuItem(icon = Icons.Filled.Settings, label = "Paramètres")

        Divider()

        // Section Support
        Text("Support", style = MaterialTheme.typography.subtitle1)
        DrawerMenuItem(icon = Icons.Filled.Info, label = "Aide")
        DrawerMenuItem(icon = Icons.Filled.ExitToApp, label = "Déconnexion")
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, label: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { /* Action à définir */ }) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Text(label)
    }
}