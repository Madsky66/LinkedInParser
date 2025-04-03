package ui.composable.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import config.GlobalConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RowScope.InputSection(applicationScope: CoroutineScope, globalConfig: GlobalConfig, onInputChange: (String) -> Unit, onProcessInput: (String) -> Unit) {
    Column(Modifier.weight(1.75f).fillMaxHeight().padding(bottom = 5.dp), Arrangement.SpaceEvenly, Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = globalConfig.pastedInput.value,
            onValueChange = {
                applicationScope.launch {
                    onInputChange(it)
                    if (!globalConfig.isExtractionLoading.value) onProcessInput(it)
                }
            },
            label = {Text("Coller le texte de la page LinkedIn ici...")},
            modifier = Modifier.weight(0.9f).fillMaxWidth().clip(RectangleShape),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = globalConfig.lightGray.value,
                focusedBorderColor = globalConfig.lightGray.value.copy(0.25f),
                unfocusedBorderColor = globalConfig.lightGray.value.copy(0.15f),
                focusedLabelColor = globalConfig.lightGray.value.copy(0.5f),
                unfocusedLabelColor = globalConfig.lightGray.value.copy(0.5f),
                placeholderColor = globalConfig.lightGray.value.copy(0.25f)
            )
        )
    }
}