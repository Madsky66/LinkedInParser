package manager

import config.GlobalInstance.config as gC
import kotlinx.coroutines.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import utils.*
import java.awt.Desktop
import java.io.*
import java.net.URI
import java.util.Locale

class FileExportManager {
    fun exportToFile(applicationScope: CoroutineScope) {
        applicationScope.launch {
            gC.isExportationLoading.value = true

            gC.fileFormat.value = if (gC.selectedOptions[0] && gC.selectedOptions[1]) {"both"} else if (gC.selectedOptions[0]) {"xlsx"} else {"csv"}
            val displayFileFormat = if (gC.fileFormat.value == "both") {"XLSX, CSV"} else {gC.fileFormat.value.replaceFirstChar {if (it. isLowerCase()) it. titlecase(Locale. getDefault()) else it. toString()}}
            gC.consoleMessage.value = ConsoleMessage("⏳ Exportation du fichier au format [$displayFileFormat] en cours...", ConsoleMessageType.INFO)

            try {
                gC.fileFullPath.value = "${gC.filePath.value}\\${gC.fileName.value}" + gC.fileFormat.value

                when (gC.fileFormat.value) {
                    "xlsx" -> {exportToXLSX()}
                    "csv" -> {exportToCSV()}
                    "both" -> {exportToXLSX(); exportToCSV()}
                    "json" -> {exportToGoogleSheets()}
                }

                gC.consoleMessage.value = ConsoleMessage("✅ Exportation du fichier au(x) format(s) [$displayFileFormat] réussie", ConsoleMessageType.SUCCESS)
                gC.consoleMessage.value = ConsoleMessage("⏳ Ouverture de Google Sheets en cours...", ConsoleMessageType.SUCCESS)

                try {
                    val sheetsUrl = "https://docs.google.com/spreadsheets/u/0/create"
                    val uri = URI(sheetsUrl)
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(uri)
                        gC.consoleMessage.value = ConsoleMessage("✅ Google Sheets ouvert. Vous pouvez maintenant importer votre fichier.", ConsoleMessageType.SUCCESS)
                    }
                }
                catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("⚠️ Exportation réussie mais impossible d'ouvrir Google Sheets : ${e.message}", ConsoleMessageType.WARNING)}
            }
            catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'exportation du fichier ${gC.fileFormat.value} : ${e.message}", ConsoleMessageType.ERROR)}
            gC.isExportationLoading.value = false
        }
    }

    fun exportToXLSX() {
        gC.fileFullPath.value = "${gC.filePath.value}\\${gC.fileName.value}" + ".xlsx"
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Prospects")
        val headers = arrayOf("LinkedIn URL", "First Name", "Middle Name", "Last Name", "Email", "Generated Emails", "Company", "Job Title")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed {index, value -> headerRow.createCell(index).setCellValue(value)}
        val row = sheet.createRow(1)
        row.createCell(0).setCellValue(gC.currentProfile.value?.linkedinUrl)
        row.createCell(1).setCellValue(gC.currentProfile.value?.firstName)
        row.createCell(2).setCellValue(gC.currentProfile.value?.middleName)
        row.createCell(3).setCellValue(gC.currentProfile.value?.lastName)
        row.createCell(4).setCellValue(gC.currentProfile.value?.email)
        row.createCell(5).setCellValue(gC.currentProfile.value?.generatedEmails?.joinToString(";"))
        row.createCell(6).setCellValue(gC.currentProfile.value?.company)
        row.createCell(7).setCellValue(gC.currentProfile.value?.jobTitle)
        FileOutputStream(gC.fileFullPath.value).use {fos -> workbook.write(fos)}
        workbook.close()
        gC.fileInstance.value = File(gC.fileFullPath.value)
    }

    fun exportToCSV() {
        gC.fileFullPath.value = "${gC.filePath.value}\\${gC.fileName.value}" + ".csv"
        File(gC.fileFullPath.value).printWriter().use {out ->
            out.println("LinkedIn URL,First Name,Middle Name,Last Name,Email,Generated Emails,Company,Job Title")
            out.println("\"${gC.currentProfile.value?.linkedinUrl}\",\"${gC.currentProfile.value?.firstName}\",\"${gC.currentProfile.value?.middleName}\",\"${gC.currentProfile.value?.lastName}\",\"${gC.currentProfile.value?.email}\",\"${gC.currentProfile.value?.generatedEmails?.joinToString(";")}\",\"${gC.currentProfile.value?.company}\",\"${gC.currentProfile.value?.jobTitle}\"")
        }
        gC.fileInstance.value = File(gC.fileFullPath.value)
    }

    fun exportToGoogleSheets() {
        val apiUrl = "https://sheets.googleapis.com/v4/spreadsheets/{spreadsheetId}/values/{range}:append?valueInputOption=RAW"
        val jsonData = """
        {
            "linkedinUrl": "${gC.currentProfile.value?.linkedinUrl}",
            "firstName": "${gC.currentProfile.value?.firstName}",
            "middleName": "${gC.currentProfile.value?.middleName}",
            "lastName": "${gC.currentProfile.value?.lastName}",
            "email": "${gC.currentProfile.value?.email}",
            "generatedEmails": "${gC.currentProfile.value?.generatedEmails?.joinToString(";")}",
            "company": "${gC.currentProfile.value?.company}",
            "jobTitle": "${gC.currentProfile.value?.jobTitle}"
        }
        """.trimIndent()
        println("Données envoyées à Google Sheets :\n----------------------------------\napiUrl : $apiUrl\n\njsonData : $jsonData")
    }
}