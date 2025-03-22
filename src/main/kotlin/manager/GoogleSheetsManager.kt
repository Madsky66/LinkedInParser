package manager

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
import data.ProspectData
import java.io.File
import java.io.FileInputStream

class GoogleSheetsManager {
    private val APPLICATION_NAME = "LinkedIn Parser Pro"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    private val TOKENS_DIRECTORY_PATH = "tokens"
    private val SPREADSHEET_ID = "VOTRE_SPREADSHEET_ID"
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)

    private fun getCredentials(): Credential {
        val credentialsFile = File("src/main/resources/extra/credentials.json")
        val clientSecrets = GoogleClientSecrets.load(
            JSON_FACTORY,
            FileInputStream(credentialsFile).reader()
        )

        val flow = GoogleAuthorizationCodeFlow.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            clientSecrets,
            SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()

        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
    }

    fun saveProspect(prospect: ProspectData) {
        try {
            val service = Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials()).setApplicationName(APPLICATION_NAME).build()
            val values = listOf(listOf(
                prospect.fullName,
                prospect.email,
                prospect.generatedEmail,
                prospect.company,
                prospect.position,
                prospect.linkedinURL,
                prospect.dateAdded
            ))
            val body = com.google.api.services.sheets.v4.model.ValueRange().setValues(values)

            service.spreadsheets().values().append(SPREADSHEET_ID, "A1", body).setValueInputOption("RAW").execute()

        }
        catch (e: Exception) {
            println("❌ Erreur lors de la sauvegarde dans Google Sheets: ${e.message}")
            throw e
        }
    }
}