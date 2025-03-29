package ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp


@Composable
fun DrawerContent(themeColors: List<Color>, pastedAPI: String, isApolloValidationLoading: Boolean, onApiKeyChange: (String) -> Unit, onProcessApiKey: (String) -> Unit) {
    val (darkGray, middleGray, lightGray) = themeColors

    Box(Modifier.fillMaxHeight().fillMaxWidth(0.25f).background(darkGray)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            // Zone de texte
            OutlinedTextField(
                value = pastedAPI,
                onValueChange = onApiKeyChange,
                modifier = Modifier.clip(RectangleShape).weight(2f),
                textStyle = TextStyle.Default,
                label = {Text("Clé API Apollo...")},
                colors = getTextFieldColors(lightGray)
            )

            // Spacer
            Spacer(Modifier.width(10.dp))

            // Bouton de validation
            Button(
                onClick = {onProcessApiKey(pastedAPI)},
                modifier = Modifier.padding(top = 8.dp).weight(0.75f).height(54.dp),
                enabled = pastedAPI.isNotBlank(),
                elevation = ButtonDefaults.elevation(10.dp),
                shape = RoundedCornerShape(0, 100, 100, 0),
                colors = getButtonColors(middleGray, darkGray, lightGray)
            ) {
                if (!isApolloValidationLoading) {Icon(Icons.Filled.Send, "")} else {CircularProgressIndicator(Modifier.align(Alignment.CenterVertically))}
            }
        }
    }
}