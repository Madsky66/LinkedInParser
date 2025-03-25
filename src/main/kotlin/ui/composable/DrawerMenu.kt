import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun DrawerMenu() {
    Column(modifier = Modifier.padding(16.dp)) {
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
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable { /* Action à définir */ }) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label)
    }
}