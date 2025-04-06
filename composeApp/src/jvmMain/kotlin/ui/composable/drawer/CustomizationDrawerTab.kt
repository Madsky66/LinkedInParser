package ui.composable.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import config.GlobalInstance.config as gC

@Composable
fun CustomizationDrawerTab() {
    Text("Options de thème", color = gC.lightGray.value, fontSize = 18.sp)
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text("Thème sombre [Expérimental...]", color = gC.lightGray.value)
        Switch(
            checked = gC.isDarkTheme.value,
            onCheckedChange = {gC.isDarkTheme.value = it},
            colors = SwitchDefaults.colors(
                checkedThumbColor = gC.lightGray.value,
                uncheckedThumbColor = gC.darkGray.value,
                checkedTrackColor = gC.lightGray.value.copy(0.5f),
                uncheckedTrackColor = gC.darkGray.value.copy(0.5f)
            )
        )
    }
}