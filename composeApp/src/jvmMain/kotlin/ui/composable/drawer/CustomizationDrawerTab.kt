package ui.composable.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import manager.AppDataManager
import ui.composable.element.SpacedDivider
import config.GlobalInstance.config as gC

@Composable
fun CustomizationDrawerTab() {
    Column(Modifier.fillMaxWidth().padding(20.dp), Arrangement.spacedBy(20.dp)) {
        Text("Options de thème", Modifier,gC.lightGray.value, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        SpacedDivider(Modifier.fillMaxWidth().background(gC.darkGray.value), "vertical", 1.dp, 10.dp, 10.dp)
        DarkThemeSwitch()
    }
}

@Composable
private fun DarkThemeSwitch() {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text("Activer / désactiver le thème sombre", color = gC.lightGray.value, style = MaterialTheme.typography.body2)
        Switch(
            checked = gC.isDarkTheme.value,
            onCheckedChange = {newValue -> gC.isDarkTheme.value = newValue; AppDataManager.updateTheme(newValue)},
            modifier = Modifier.semantics {contentDescription = "Basculer entre le thème clair et sombre"},
            colors = SwitchDefaults.colors(
                checkedThumbColor = gC.lightGray.value,
                uncheckedThumbColor = gC.darkGray.value,
                checkedTrackColor = gC.lightGray.value.copy(0.5f),
                uncheckedTrackColor = gC.darkGray.value.copy(0.5f)
            )
        )
    }
}