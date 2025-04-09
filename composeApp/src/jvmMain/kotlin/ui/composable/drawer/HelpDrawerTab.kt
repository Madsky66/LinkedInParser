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
fun HelpDrawerTab() {
    Text("Documentation", color = gC.lightGray.value, fontSize = 18.sp)
    Spacer(Modifier.height(10.dp))
    Text("Pour utiliser cette application, entrez le lien URL d'un profil LinkedIn (attention à bien être sur le profil et non dans une page de recherche ou autre) et collez-le dans la zone de texte à gauche, ensuite, appuyez sur le bouton pour lancer l'extraction.", color = gC.lightGray.value)
}