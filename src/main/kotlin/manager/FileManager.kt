import data.ProspectData
import org.slf4j.LoggerFactory
import java.io.File

class FileManager {
//    private val logger = LoggerFactory.getLogger(FileManager::class.java)

    fun exportToCSV(profile: ProspectData, filePath: String) {
        val file = File(filePath)
        file.printWriter().use {out ->
            out.println("First name,Last name,LinkedIn URL,Job Title,Email,Generated Emails")
            out.println("${profile.firstName},${profile.lastName},${profile.linkedinURL},${profile.jobTitle},${profile.email},${profile.generatedEmails.joinToString("; ")}")
        }
    }

//    suspend fun writeProspectToSheet(spreadsheetId: String, prospectData: ProspectData) {
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
//    }
}