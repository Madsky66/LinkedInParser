import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import data.ProspectData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader

class GoogleSheetsManager {
    private val APPLICATION_NAME = "LinkedIn Parser"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    private val TOKENS_DIRECTORY_PATH = "tokens"
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)
    private val CREDENTIALS_FILE_PATH = "extra/credentials.json"
    private val logger = LoggerFactory.getLogger(GoogleSheetsManager::class.java)

    private var service: Sheets? = null

    init {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
//        service = Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport)).setApplicationName(APPLICATION_NAME).build()
    }

    fun exportToCSV(profile: ProspectData, filePath: String) {
        val file = File(filePath)
        file.printWriter().use {out ->
            out.println("First Name,Last Name,LinkedIn URL,Position,Emails")
            out.println("${profile.firstName},${profile.lastName},${profile.linkedinURL},${profile.position},${profile.generatedEmails.joinToString("; ")}")
        }
    }

    @Throws(Exception::class)
    private fun getCredentials(httpTransport: com.google.api.client.http.HttpTransport): Credential {
        val inputStream = GoogleSheetsManager::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH) ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
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
                        prospectData.position,
                        prospectData.linkedinURL,
                        prospectData.dateAdded.toString()
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