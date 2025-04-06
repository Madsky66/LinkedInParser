package ui.composable.drawer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import config.GlobalInstance.config as gC


@Composable
fun ContactDrawerTab() {
    Text("Nous contacter", color = gC.lightGray.value, fontSize = 18.sp)
    Spacer(Modifier.height(10.dp))
    Text("Email : pmbussy66@gmail.com", color = gC.lightGray.value)
}