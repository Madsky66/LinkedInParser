import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import data.ProspectData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File

class GoogleSheetsManager {
    private val logger = LoggerFactory.getLogger(GoogleSheetsManager::class.java)

    private var service: Sheets? = null

    fun exportToCSV(profile: ProspectData, filePath: String) {
        val file = File(filePath)
        file.printWriter().use {out ->
            out.println("First name,Last name,LinkedIn URL,Job Title,Email,Generated Emails")
            out.println("${profile.firstName},${profile.lastName},${profile.linkedinURL},${profile.jobTitle},${profile.email},${profile.generatedEmails.joinToString("; ")}")
        }
    }

    suspend fun writeProspectToSheet(spreadsheetId: String, prospectData: ProspectData) {
        withContext(Dispatchers.IO) {
            try {
                val range = "Prospects!A:G"
                val valueRange = ValueRange()
                val values = listOf(
                    listOf(
                        prospectData.fullName,
                        prospectData.firstName,
                        prospectData.lastName,
                        prospectData.company,
                        prospectData.jobTitle,
                        prospectData.linkedinURL,
                    )
                )
                valueRange.setValues(values)
                val appendRequest = service!!.spreadsheets().values().append(spreadsheetId, range, valueRange)
                appendRequest.valueInputOption = "USER_ENTERED"
                val response = appendRequest.execute()
                logger.info("✅ Prospect saved to Google Sheets: ${prospectData.fullName}, ${response.updates.updatedCells} cells updated")
                println(response)

            }
            catch (e: Exception) {
                logger.error("❌ Erreur lors de la sauvegarde dans Google Sheets: ${e.message}", e)
                println("Error writing to sheet: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }
}