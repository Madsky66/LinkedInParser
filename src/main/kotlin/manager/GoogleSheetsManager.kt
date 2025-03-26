package manager

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import data.ProspectData
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.GeneralSecurityException

object GoogleSheetsManager {
    private const val APPLICATION_NAME = "LinkedIn Parser"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    private const val TOKENS_DIRECTORY_PATH = "tokens"
    private val SPREADSHEET_ID = System.getenv("GOOGLE_SHEET_ID") ?: "YOUR_DEFAULT_SPREADSHEET_ID"
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)
    private var sheetsService: Sheets? = null
    private val logger = LoggerFactory.getLogger(GoogleSheetsManager::class.java)

    fun getSheetsService(): Sheets? {
        if (sheetsService == null) {
            try {
                val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                val credential = getCredentials()
                sheetsService = Sheets.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build()
            }
            catch (e: GeneralSecurityException) {e.printStackTrace()}
            catch (e: IOException) {e.printStackTrace()}
        }
        return sheetsService
    }

    private fun getCredentials(): Credential {
        val credentialsFile = File("src/main/resources/extra/credentials.json")
        if (!credentialsFile.exists()) {throw IllegalStateException("Credentials file not found: ${credentialsFile.absolutePath}")}
        try {
            FileInputStream(credentialsFile).use {fileStream ->
                val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, fileStream.reader())
                val flow = GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build()
                return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
            }
        }
        catch (e: IOException) {
            logger.error("Error loading credentials file", e)
            throw e
        }
    }

    fun saveProspect(prospect: ProspectData, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials()).setApplicationName(APPLICATION_NAME).build()
                val values = listOf(
                    listOf(
                        prospect.fullName,
                        prospect.email,
                        prospect.generatedEmail,
                        prospect.company,
                        prospect.position,
                        prospect.linkedinURL,
                        prospect.dateAdded
                    )
                )
                val body = com.google.api.services.sheets.v4.model.ValueRange().setValues(values)
                val response = service.spreadsheets().values().append(SPREADSHEET_ID, "A1", body).setValueInputOption("RAW").execute()
                logger.info("✅ Prospect saved to Google Sheets: ${response.updates.updatedCells} cells updated")
            }
            catch (e: Exception) {logger.error("❌ Erreur lors de la sauvegarde dans Google Sheets: ${e.message}", e)}
        }
    }
}