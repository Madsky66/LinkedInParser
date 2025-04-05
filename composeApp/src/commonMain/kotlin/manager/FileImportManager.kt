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
    fun importFromFile(applicationScope: CoroutineScope) {
        if (gC.fileInstance.value != null) {
            applicationScope.launch {
                gC.consoleMessage.value = ConsoleMessage("⏳ Importation du fichier ${gC.fileName.value}.${gC.fileFormat.value}...", ConsoleMessageType.INFO)
                gC.isImportationLoading.value = true
                try {
                    if (gC.fileInstance.value == null) {throw IllegalArgumentException("Le fichier spécifié n'existe pas : ${gC.filePath.value}")}
                    var numberOfColumns = 0
                    when (gC.fileFormat.value) {
                        "xlsx" -> importFromXLSX() {numberOfColumns = it}
                        "csv" -> importFromCSV() {numberOfColumns = it}
                        "json" -> importFromGoogleSheets() {numberOfColumns = it}
                        else -> throw IllegalArgumentException("Format non supporté: ${gC.fileFormat.value}")
                    }
                    gC.consoleMessage.value =
                        when (numberOfColumns) {
                            0 -> ConsoleMessage("❌ Le profil importé est vide", ConsoleMessageType.ERROR)
                            1,2,3,4,5,6,7 -> ConsoleMessage("⚠️ Le profil importé est incomplet", ConsoleMessageType.WARNING)
                            else -> ConsoleMessage("✅ Importation du fichier ${gC.fileName.value}.${gC.fileFormat.value} réussie", ConsoleMessageType.SUCCESS)
                        }
                }
                catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'importation du fichier ${gC.fileFormat.value} : ${e.message}", ConsoleMessageType.ERROR)}
                gC.isImportationLoading.value = false
            }
        }
        else {gC.consoleMessage.value = ConsoleMessage("⚠️ Aucun fichier sélectionné", ConsoleMessageType.WARNING)}
    }

    private fun importFromXLSX(onImportedFile: (Int) -> Unit) {
        FileInputStream(gC.fileInstance.value!!).use {fis ->
            val workbook = XSSFWorkbook(fis)
            val sheet = workbook.getSheetAt(0)
            for (row in sheet.drop(1)) {
                val columns = row.map {it?.stringCellValue?.trim() ?: ""}
                val linkedinURL = columns.getOrNull(0)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                val firstName = columns.getOrNull(1)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                val middleName = columns.getOrNull(2)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                val lastName = columns.getOrNull(3)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                val email = columns.getOrNull(4)?.trim()?.takeIf {it.isNotBlank() && it != "null" }?: ""
                val generatedEmails = columns.getOrNull(5)?.split(";")?.map {it.trim()}?.filter {it.isNotEmpty() && it != "null"} ?: emptyList()
                val company = columns.getOrNull(6)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                val jobTitle = columns.getOrNull(7)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
                val fullname = "$firstName${if (middleName != "") {" $middleName "} else {" "}}$lastName"
                val prospect = ProspectData(linkedinURL, fullname, firstName, middleName, lastName, email, generatedEmails = generatedEmails, company = company, jobTitle = jobTitle)
                gC.currentProfile.value = prospect
                onImportedFile(columns.size)
            }
            workbook.close()
        }
    }

    private fun importFromCSV(onImportedFile: (Int) -> Unit) {
        gC.fileInstance.value!!.readLines().drop(1).forEach {line ->
            val columns = line.split(",").map {it.trim().removeSurrounding("\"")}
            val linkedinURL = columns.getOrNull(0)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val firstName = columns.getOrNull(1)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val middleName = columns.getOrNull(2)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val lastName = columns.getOrNull(3)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val email = columns.getOrNull(4)?.trim()?.takeIf {it.isNotBlank() && it != "null" }?: ""
            val generatedEmails = columns.getOrNull(5)?.split(";")?.map {it.trim()}?.filter {it.isNotEmpty() && it != "null"} ?: emptyList()
            val company = columns.getOrNull(6)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val jobTitle = columns.getOrNull(7)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val fullname = "$firstName${if (middleName != "") {" $middleName "} else {" "}}$lastName"
            val prospect = ProspectData(linkedinURL, fullname, firstName, middleName, lastName, email, generatedEmails = generatedEmails, company = company, jobTitle = jobTitle)
            gC.currentProfile.value = prospect
            onImportedFile(columns.size)
        }
    }

    private fun importFromGoogleSheets(onImportedFile: (Int) -> Unit) {
    }
}