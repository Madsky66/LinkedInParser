package manager

import config.GlobalInstance.config as gC
import data.ProspectData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import utils.ConsoleMessage
import utils.ConsoleMessageType
import java.io.*

class FileImportManager {
    fun importFromFile(applicationScope: CoroutineScope): ProspectData {
        if (gC.fileInstance.value != null) {
            applicationScope.launch {
                gC.consoleMessage.value = ConsoleMessage("⏳ Importation du fichier ${gC.fileName.value}.${gC.fileFormat.value}...", ConsoleMessageType.INFO)
                gC.isImportationLoading.value = true
                try {
                    if (gC.fileInstance.value == null) {throw IllegalArgumentException("Le fichier spécifié n'existe pas : ${gC.filePath.value}")}
                    var numberOfColumns = 0
                    FileInputStream(gC.fileInstance.value!!).use {fis ->
                        val workbook = XSSFWorkbook(fis)
                        val sheet = workbook.getSheetAt(0)
                        for (row in sheet.drop(1)) {
                            val columns = row.map {it?.stringCellValue?.trim() ?: ""}
                            val company = columns.getOrNull(0)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                            val firstName = columns.getOrNull(1)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                            val lastName = columns.getOrNull(2)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                            val jobTitle = columns.getOrNull(3)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                            val email = columns.getOrNull(4)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                            val phoneNumber = columns.getOrNull(5)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                            val linkedinURL = columns.getOrNull(6)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                            val prospect = ProspectData(company, "", firstName, "", lastName, jobTitle, email, phoneNumber, linkedinURL, emptyList())
                            gC.currentProfile.value = prospect
                            numberOfColumns = columns.size
                        }
                        workbook.close()
                    }
                    gC.consoleMessage.value =
                        when (numberOfColumns) {
                            0 -> ConsoleMessage("❌ Le profil importé est vide", ConsoleMessageType.ERROR)
                            1,2,3,4,5,6,7 -> ConsoleMessage("⚠️ Le profil importé est incomplet", ConsoleMessageType.WARNING)
                            else -> {ConsoleMessage("✅ Importation du fichier ${gC.fileName.value}.${gC.fileFormat.value} réussie", ConsoleMessageType.SUCCESS)}
                        }
                    return@launch gC.currentProfile.value!!
                }
                catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'importation du fichier ${gC.fileFormat.value} : ${e.message}", ConsoleMessageType.ERROR); return@launch gC.currentProfile.value!!}
                gC.isImportationLoading.value = false
            }
        }
        else {gC.consoleMessage.value = ConsoleMessage("⚠️ Aucun fichier sélectionné", ConsoleMessageType.WARNING); return gC.currentProfile.value!!}
        return TODO("Provide the return value")
    }

    fun importFromGoogleSheets(sheetsId: String): ProspectData {
        return TODO("Provide the return value")
    }
}