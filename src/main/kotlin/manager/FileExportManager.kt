package manager

import config.GlobalConfig
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class FileExportManager {
    fun exportToFile(gC: GlobalConfig, exportFilePath: String) {
        val extension = exportFilePath.substringAfterLast('.', "").lowercase()
        when (extension) {
            "csv" -> exportToCSV(gC, exportFilePath)
            "xlsx" -> exportToXLSX(gC, exportFilePath)
            "json" -> exportToGoogleSheets(gC)
            else -> throw IllegalArgumentException("Format de fichier non supporté: $extension")
        }
    }

    private fun exportToCSV(gC: GlobalConfig, exportFilePath: String) {
        File(exportFilePath).printWriter().use {out ->
            out.println("LinkedIn URL,First Name,Middle Name,Last Name,Email,Generated Emails,Company,Job Title")
            out.println("\"${gC.currentProfile.value?.linkedinUrl}\",\"${gC.currentProfile.value?.firstName}\",\"${gC.currentProfile.value?.middleName}\",\"${gC.currentProfile.value?.lastName}\",\"${gC.currentProfile.value?.email}\",\"${gC.currentProfile.value?.generatedEmails?.joinToString(";")}\",\"${gC.currentProfile.value?.company}\",\"${gC.currentProfile.value?.jobTitle}\"")
        }
    }

    private fun exportToXLSX(gC: GlobalConfig, exportFilePath: String) {
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
        FileOutputStream(exportFilePath).use {fos -> workbook.write(fos)}
        workbook.close()
    }

    private fun exportToGoogleSheets(gC: GlobalConfig) {
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