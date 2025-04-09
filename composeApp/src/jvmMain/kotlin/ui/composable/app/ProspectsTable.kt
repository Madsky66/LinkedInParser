package ui.composable.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.ProspectData
import config.GlobalInstance.config as gC

@Composable
fun RowScope.ProspectsTable(prospects: List<ProspectData>, onProspectSelected: (ProspectData) -> Unit) {
    val lazyListState = rememberLazyListState()
    Column(Modifier.weight(1f).fillMaxHeight().padding(5.dp, 5.dp, 0.dp, 0.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
        Column(Modifier.fillMaxSize().background(gC.darkGray.value).border(1.dp, gC.lightGray.value)) {
            // En-têtes du tableau
            Row(Modifier.fillMaxWidth().background(gC.middleGray.value).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TableHeader("Société", 0.18f)
                TableHeader("Prénom", 0.12f)
                TableHeader("Nom", 0.12f)
                TableHeader("Poste", 0.20f)
                TableHeader("Email", 0.18f)
                TableHeader("Téléphone", 0.10f)
                TableHeader("LinkedIn", 0.10f)
            }
            Divider(color = gC.lightGray.value, thickness = 1.dp)
            // Contenu du tableau
            if (prospects.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {Text("Aucun prospect à afficher. Importez un fichier pour commencer.", color = gC.lightGray.value, fontSize = 16.sp)}
            }
            else {
                LazyColumn(Modifier.fillMaxSize(), lazyListState) {
                    items(prospects) {prospect ->
                        ProspectRow(prospect, onProspectSelected)
                        Divider(color = gC.middleGray.value.copy(0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader(text: String, widthFraction: Float) {Text(text, Modifier.fillMaxWidth(widthFraction).padding(horizontal = 4.dp), gC.lightGray.value, 14.sp, fontWeight = FontWeight.Bold)}

@Composable
private fun ProspectRow(prospect: ProspectData, onProspectSelected: (ProspectData) -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp).padding(8.dp).background(Color.Transparent).clickable {onProspectSelected(prospect)}, verticalAlignment = Alignment.CenterVertically) {
        TableCell(prospect.company, 0.18f)
        TableCell(prospect.firstName, 0.12f)
        TableCell(prospect.lastName, 0.12f)
        TableCell(prospect.jobTitle, 0.20f)
        TableCell(prospect.email, 0.18f)
        TableCell(prospect.phoneNumber, 0.10f)
        TableCell(prospect.linkedinUrl, 0.10f)
    }
}

@Composable
private fun TableCell(text: String, widthFraction: Float) {Text(text, Modifier.fillMaxWidth(widthFraction).padding(horizontal = 4.dp), gC.lightGray.value, 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)}