import data.ProspectData
import utils.FileFormat
import java.io.File

class FileManager {

    fun importFromFile(filePath: String, importFileFormat: FileFormat?, onIncompleteProspectData: (Boolean) -> Unit) {
        val fileToImport = File(filePath)
        if (!fileToImport.exists()) {throw IllegalArgumentException("Le fichier spécifié n'existe pas : $filePath")}
        when (importFileFormat) {
            FileFormat.CSV -> {
                File(filePath).readLines().drop(1).mapNotNull {line ->
                    val columns = line.split(",")
                    if (columns.size >= 9) {
                        ProspectData(
                            linkedinURL = "${columns[2].takeIf {it.isNotBlank()}}",
                            fullName = "${columns[0].takeIf {it.isNotBlank()}} ${columns[1].takeIf {it.isNotBlank()}} ${columns[2].takeIf {it.isNotBlank()}}",
                            firstName = "${columns[0].takeIf {it.isNotBlank()}}",
                            middleName = "${columns[1].takeIf {it.isNotBlank()}}",
                            lastName = "${columns[2].takeIf {it.isNotBlank()}}",
                            email = "${columns[4].takeIf {it.isNotBlank()}}",
                            generatedEmails = columns[5].split(";").map {it.trim()}.filter {it.isNotEmpty()},
                            company = "${columns[6].takeIf {it.isNotBlank()}}",
                            jobTitle = "${columns[3].takeIf {it.isNotBlank()}}",
                        )
                        onIncompleteProspectData(false)
                    }
                    else {
                        onIncompleteProspectData(true)
                        null
                    }
                }
            }
            FileFormat.XLSX -> {}
            else -> {print("Le format du fichier est incorrect")}
        }
    }

   /*suspend*/ fun exportToFile(/*spreadsheetId: String, */prospectData: ProspectData, filePath: String, exportFileFormat: FileFormat) {
        when (exportFileFormat) {
            FileFormat.CSV -> {
                val fileToExport = File(filePath)
                fileToExport.printWriter().use {out ->
                    out.println("First name,Middle Name,Last name,LinkedIn URL,Email,Generated Emails,Company,Job Title")
                    out.println(
                        "${prospectData.firstName}," +
                                "${prospectData.middleName}," +
                                "${prospectData.lastName}," +
                                "${prospectData.linkedinURL}," +
                                "${prospectData.email}," +
                                "${prospectData.generatedEmails.joinToString(";")}," +
                                "${prospectData.company}," +
                                prospectData.jobTitle
                    )
                }
            }
            FileFormat.XLSX -> {val fileToExport = File(filePath)}
            else -> {}
        }
//        withContext(Dispatchers.IO) {
//            try {
//                val range = "Prospects!A:G"
//                val valueRange = ValueRange()
//                val values = listOf(
//                    listOf(
//                        prospectData.fullName,
//                        prospectData.firstName,
//                        prospectData.lastName,
//                        prospectData.company,
//                        prospectData.jobTitle,
//                        prospectData.linkedinURL,
//                    )
//                )
//                valueRange.setValues(values)
//                val appendRequest = service!!.values().append(spreadsheetId, range, valueRange)
//                appendRequest.valueInputOption = "USER_ENTERED"
//                logger.info("✅ Prospect saved to Google Sheets: ${prospectData.fullName}")
//
//            }
//            catch (e: Exception) {
//                logger.error("❌ Erreur lors de la sauvegarde dans Google Sheets: ${e.message}", e)
//                e.printStackTrace()
//            }
//        }
   }
}
