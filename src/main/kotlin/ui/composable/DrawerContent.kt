package ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContent(themeColors: List<Color>, pastedAPI: String, apiKey: String, isApolloValidationLoading: Boolean, onApiKeyChange: (String) -> Unit, onProcessApiKey: (String) -> Unit) {
    var showConfirmModal by remember {mutableStateOf(false)}
    val (darkGray, middleGray, lightGray) = themeColors
    val confirmMessage = "Êtes-vous certain(e) de vouloir utiliser la clé API [Apollo] suivante ?\n\n----- $pastedAPI -----"

    // Modale de confirmation
    if (showConfirmModal) {
        CustomConfirmModal(
            themeColors, pastedAPI, confirmMessage,
            firstButtonText = "Annuler",
            secondButtonText = "Confirmer",
            onSecondButtonClick = {
                onProcessApiKey(pastedAPI)
                showConfirmModal = false
            },
            onDismissRequest = {showConfirmModal = false}
        )
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            // Zone de texte
            OutlinedTextField(
                value = pastedAPI,
                onValueChange = onApiKeyChange,
                modifier = Modifier.clip(RectangleShape).weight(2f),
                textStyle = TextStyle.Default,
                label = {Text("Clé API Apollo...")},
                maxLines = 1,
                colors = getTextFieldColors(lightGray)
            )

            // Spacer
            Spacer(Modifier.width(10.dp))

            // Bouton de validation
            Button(
                onClick = {showConfirmModal = true},
                modifier = Modifier.padding(top = 8.dp).weight(0.75f).height(54.dp),
                enabled = pastedAPI.isNotBlank(),
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(0, 100, 100, 0),
                colors = getButtonColors(middleGray, darkGray, lightGray)
            ) {
                if (!isApolloValidationLoading) {Icon(Icons.Filled.Send, "")} else {CustomProgressIndicator(themeColors)}
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().border(BorderStroke(1.dp, darkGray)).padding(20.dp, 10.dp), Arrangement.SpaceBetween) {
            val text = if (apiKey.isBlank()) {"Aucune clé validée"} else {apiKey}
            Text("Clé actuelle : ", color = lightGray)
            Text(text, color = if (apiKey.isBlank()) {lightGray} else {Color.Green.copy(0.5f)})
        }
    }
}