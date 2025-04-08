package manager

import data.ProspectData
import config.GlobalInstance.config as gC
import kotlinx.coroutines.*
import utils.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.google.api.services.sheets.v4.model.*

class FileExportManager {
    fun exportToFile(applicationScope: CoroutineScope) {
        applicationScope.launch {
            gC.isExportationLoading.value = true
            try {
                gC.fileFullPath.value = "${gC.filePath.value}\\${gC.fileName.value}.xlsx"
                val file = File(gC.fileFullPath.value)
                if (file.exists()) {if (!validateExcelFile(file)) {gC.consoleMessage.value = ConsoleMessage("❌ Le fichier existant est incompatible avec le modèle attendu.", ConsoleMessageType.ERROR); return@launch}}
                else {createExcelFile(file)}
                when (gC.fileFormat.value) {
                    "xlsx", "both" -> {updateExcelFile(file, listOf(gC.currentProfile.value!!));  if (gC.fileFormat.value == "both") {exportToGoogleSheets()}}
                    "sync" -> exportToGoogleSheets()
                }
                gC.consoleMessage.value = ConsoleMessage("✅ Exportation du fichier au format [${gC.fileFormat.value}] réussie", ConsoleMessageType.SUCCESS)
            }
            catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'exportation : ${e.message}", ConsoleMessageType.ERROR)}
            finally {gC.isExportationLoading.value = false}
        }
    }

    private fun validateExcelFile(file: File): Boolean {
        return try {
            FileInputStream(file).use {fis ->
                val workbook = XSSFWorkbook(fis)
                val sheet = workbook.getSheet("Prospects") ?: return false
                val headerRow = sheet.getRow(0) ?: return false
                val expectedHeaders = listOf("SOCIETE", "PRENOM", "NOM", "POSTE", "EMAIL", "TEL", "LINKEDIN")
                for (i in expectedHeaders.indices) {if (headerRow.getCell(i)?.stringCellValue != expectedHeaders[i]) {return false}}
            }
            true
        }
        catch (e: Exception) {false}
    }

    private fun createExcelFile(file: File) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Prospects")
        val headerRow = sheet.createRow(0)
        val headers = listOf("SOCIETE", "PRENOM", "NOM", "POSTE", "EMAIL", "TEL", "LINKEDIN")
        headers.forEachIndexed {index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            val style = workbook.createCellStyle()
            val font = workbook.createFont()
            font.bold = true
            style.setFont(font)
            cell.cellStyle = style
        }
        FileOutputStream(file).use {fos -> workbook.write(fos)}
        workbook.close()
    }

    private fun updateExcelFile(file: File, prospects: List<ProspectData>) {
        FileInputStream(file).use {fis ->
            val workbook = XSSFWorkbook(fis)
            val sheet = workbook.getSheet("Prospects") ?: return
            val lastRowNum = sheet.lastRowNum
            for ((index, prospect) in prospects.withIndex()) {
                val row = sheet.createRow(lastRowNum + 1 + index)
                row.createCell(0).setCellValue(prospect.company)
                row.createCell(1).setCellValue(prospect.firstName)
                row.createCell(2).setCellValue(prospect.middleName)
                row.createCell(3).setCellValue(prospect.lastName)
                row.createCell(4).setCellValue(prospect.email)
                row.createCell(6).setCellValue(prospect.linkedinUrl)
            }
            FileOutputStream(file).use {fos -> workbook.write(fos)}
            workbook.close()
        }
    }

    fun exportToGoogleSheets() {
        try {
            val sheetsService = GoogleSheetsHelper.getSheetsService("composeApp/src/jvmMain/composeResources/file/client_secret.json")
            val spreadsheetId = "votre_spreadsheet_id" // Remplacez par l'ID de votre feuille Google Sheets
            val range = "Prospects!A1"
            val values = mutableListOf(listOf("SOCIETE", "PRENOM", "NOM", "POSTE", "EMAIL", "TEL", "LINKEDIN"))
            gC.currentProfile.value?.let {prospect -> values.add(listOf(prospect.company, prospect.firstName, prospect.middleName, prospect.lastName, prospect.email, prospect.linkedinUrl))}
            val body = ValueRange().setValues(values)
            sheetsService.spreadsheets().values().append(spreadsheetId, range, body).setValueInputOption("RAW").execute()
            gC.consoleMessage.value = ConsoleMessage("✅ Données synchronisées avec Google Sheets.", ConsoleMessageType.SUCCESS)
        }
        catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de la synchronisation avec Google Sheets : ${e.message}", ConsoleMessageType.ERROR)}
    }
}