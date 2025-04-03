package manager

import data.ProspectData
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*

class FileImportManager {
    fun importFromFile(importFilePath: String, onImportedProspectData: (ProspectData, Int) -> Unit) {
        val fileToImport = File(importFilePath)
        if (!fileToImport.exists()) {throw IllegalArgumentException("Le fichier spécifié n'existe pas : $importFilePath")}
        val extension = fileToImport.extension.lowercase()
        when (extension) {
            "xlsx" -> importFromXLSX(fileToImport, onImportedProspectData)
            "csv" -> importFromCSV(fileToImport, onImportedProspectData)
            "json" -> importFromGoogleSheets(fileToImport, onImportedProspectData)
            else -> throw IllegalArgumentException("Format non supporté: $extension")
        }
    }

    private fun importFromXLSX(file: File, onImportedProspectData: (ProspectData, Int) -> Unit) {
        FileInputStream(file).use {fis ->
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
                val prospect = ProspectData(linkedinURL, firstName, middleName, lastName, email, generatedEmails = generatedEmails, company = company, jobTitle = jobTitle)
                onImportedProspectData(prospect, columns.size)
            }
            workbook.close()
        }
    }

    private fun importFromCSV(file: File, onImportedProspectData: (ProspectData, Int) -> Unit) {
        file.readLines().drop(1).forEach {line ->
            val columns = line.split(",").map {it.trim().removeSurrounding("\"")}
            val linkedinURL = columns.getOrNull(0)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val firstName = columns.getOrNull(1)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val middleName = columns.getOrNull(2)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val lastName = columns.getOrNull(3)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val email = columns.getOrNull(4)?.trim()?.takeIf {it.isNotBlank() && it != "null" }?: ""
            val generatedEmails = columns.getOrNull(5)?.split(";")?.map {it.trim()}?.filter {it.isNotEmpty() && it != "null"} ?: emptyList()
            val company = columns.getOrNull(6)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val jobTitle = columns.getOrNull(7)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val prospect = ProspectData(linkedinURL, firstName, middleName, lastName, email, generatedEmails = generatedEmails, company = company, jobTitle = jobTitle)
            onImportedProspectData(prospect, columns.size)
        }
    }

    private fun importFromGoogleSheets(file: File, function: (ProspectData, Int) -> Unit) {
    }
}