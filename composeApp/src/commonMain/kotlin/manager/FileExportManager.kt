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
                if (gC.currentProfile.value == null) {gC.consoleMessage.value = ConsoleMessage("❌ Aucun profil chargé pour l'exportation", ConsoleMessageType.ERROR); return@launch}
                if (gC.filePath.value.isBlank() || gC.fileName.value.isBlank()) {gC.consoleMessage.value = ConsoleMessage("❌ Chemin ou nom de fichier non spécifié", ConsoleMessageType.ERROR); return@launch}
                val exportToExcel = gC.selectedOptions[0]
                val exportToGoogleSheets = gC.selectedOptions.getOrNull(2) == true
                if (exportToExcel) {
                    gC.fileFullPath.value = "${gC.filePath.value}\\${gC.fileName.value}.xlsx"
                    val file = File(gC.fileFullPath.value)
                    if (file.exists()) {
                        if (!validateExcelFile(file)) {
                            gC.consoleMessage.value = ConsoleMessage("❌ Le fichier cible spécifié est incompatible avec le modèle attendu.", ConsoleMessageType.ERROR)
                            return@launch
                        }
                    }
                    else {createExcelFile(file)}
                    updateExcelFile(file, listOf(gC.currentProfile.value!!))
                }
                if (exportToGoogleSheets) {exportToGoogleSheets()}
                gC.consoleMessage.value = ConsoleMessage("✅ Exportation terminée avec succès", ConsoleMessageType.SUCCESS)
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
                true
            }
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
                row.createCell(2).setCellValue(prospect.lastName)
                row.createCell(3).setCellValue(prospect.jobTitle)
                row.createCell(4).setCellValue(prospect.email)
                row.createCell(5).setCellValue(prospect.phoneNumber)
                row.createCell(6).setCellValue(prospect.linkedinUrl)
            }
            FileOutputStream(file).use {fos -> workbook.write(fos)}
            workbook.close()
        }
    }

    fun exportToGoogleSheets() {
        try {
            val spreadsheetId = gC.googleSheetsId.value
            if (spreadsheetId.isBlank()) {gC.consoleMessage.value = ConsoleMessage("❌ ID de feuille Google Sheets non configuré", ConsoleMessageType.ERROR); return}
            val sheetsService = GoogleSheetsHelper.getSheetsService()
            val range = "Prospects!A1"
            val spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute()
            val sheetExists = spreadsheet.sheets.any {it.properties.title == "Prospects"}
            if (!sheetExists) {
                val addSheetRequest = AddSheetRequest().setProperties(SheetProperties().setTitle("Prospects"))
                val batchUpdateRequest = BatchUpdateSpreadsheetRequest().setRequests(listOf(Request().setAddSheet(addSheetRequest)))
                sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute()
                val headers = listOf(listOf("SOCIETE", "PRENOM", "NOM", "POSTE", "EMAIL", "TEL", "LINKEDIN"))
                val headerBody = ValueRange().setValues(headers)
                sheetsService.spreadsheets().values().update(spreadsheetId, "Prospects!A1", headerBody).setValueInputOption("RAW").execute()
            }
            val prospect = gC.currentProfile.value ?: return
            val values = listOf(listOf(prospect.company, prospect.firstName, prospect.lastName, prospect.jobTitle, prospect.email, prospect.phoneNumber, prospect.linkedinUrl))
            val body = ValueRange().setValues(values)
            sheetsService.spreadsheets().values().append(spreadsheetId, "Prospects!A2", body).setValueInputOption("RAW").setInsertDataOption("INSERT_ROWS").execute()
            gC.consoleMessage.value = ConsoleMessage("✅ Données synchronisées avec Google Sheets.", ConsoleMessageType.SUCCESS)
        }
        catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de la synchronisation avec Google Sheets : ${e.message}", ConsoleMessageType.ERROR)}
    }
}