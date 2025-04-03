package ui.composable.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import config.GlobalConfig

@Composable
fun ColumnScope.StatusBar(gC: GlobalConfig, statusColor: Color) {
    // Console de statut
    Column(Modifier.weight(0.1f).fillMaxWidth().background(Color.Black), Arrangement.Center) {
        Text(gC.consoleMessage.value.message, Modifier.padding(20.dp, 10.dp), fontSize = 15.sp, color = statusColor)
    }
}