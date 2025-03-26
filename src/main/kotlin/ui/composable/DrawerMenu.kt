package ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

data class MenuItem(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
fun DrawerMenu() {
    Column(Modifier.fillMaxHeight().background(Color.LightGray).padding(10.dp)) {
        Button(onClick = {/*TODO*/}, Modifier.width(200.dp).padding(PaddingValues(vertical = 5.dp)), colors = ButtonDefaults.buttonColors(Color.Gray)) {
            Text("Dashboard", color = Color.White)
        }
        Button(onClick = {/*TODO*/}, Modifier.width(200.dp).padding(PaddingValues(vertical = 5.dp)), colors = ButtonDefaults.buttonColors(Color.Gray)) {
            Text("Prospects", color = Color.White)
        }
        Button(onClick = {/*TODO*/}, Modifier.width(200.dp).padding(PaddingValues(vertical = 5.dp)), colors = ButtonDefaults.buttonColors(Color.Gray)) {
            Text("Settings", color = Color.White)
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = {/*TODO*/}, Modifier.width(200.dp).padding(PaddingValues(vertical = 5.dp)), colors = ButtonDefaults.buttonColors(Color.DarkGray)) {
            Text("Import CSV", color = Color.White)
        }
        Button(onClick = {/*TODO*/}, Modifier.width(200.dp).padding(PaddingValues(vertical = 5.dp)), colors = ButtonDefaults.buttonColors(Color.DarkGray)) {
            Text("Export CSV", color = Color.White)
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = {/*TODO*/}, Modifier.width(200.dp).padding(PaddingValues(vertical = 5.dp)), colors = ButtonDefaults.buttonColors(Color.Red)) {
            Text("Quit", color = Color.White)
        }
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