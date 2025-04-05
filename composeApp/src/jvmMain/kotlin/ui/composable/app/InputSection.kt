package ui.composable.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import config.GlobalInstance.config as gC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ui.composable.effect.CustomOutlinedTextFieldColors

@Composable
fun RowScope.InputSection(applicationScope: CoroutineScope) {
    Column(Modifier.weight(1.75f).fillMaxHeight().padding(bottom = 5.dp), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = gC.pastedInput.value,
            onValueChange = {applicationScope.launch {gC.pastedInput.value = it; if (!gC.isExtractionLoading.value) gC.linkedinManager.processInput(applicationScope, it)}},
            label = {Text("Coller le texte de la page LinkedIn ici...")},
            modifier = Modifier.weight(0.9f).fillMaxWidth().clip(RectangleShape),
            colors = CustomOutlinedTextFieldColors()
        )
    }
}