package utils

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import config.GlobalInstance.config as gC
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader

object GoogleSheetsHelper {
    private const val APPLICATION_NAME = "LinkedInScraper"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE_METADATA_READONLY)
    private const val TOKENS_DIRECTORY_PATH = "tokens"
    private const val CLIENT_SECRET_FILE_PATH = "src/jvmMain/composeResources/file/client_secret.json"
    private const val LOCAL_SERVER_PORT = 8888

    fun getSheetsService(): Sheets {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credentials = getCredentials(httpTransport)
        return Sheets.Builder(httpTransport, JSON_FACTORY, credentials).setApplicationName(APPLICATION_NAME).build()
    }

    private fun getCredentials(httpTransport: NetHttpTransport): Credential {
        val clientSecretFile = File(CLIENT_SECRET_FILE_PATH)
        if (!clientSecretFile.exists()) {throw FileNotFoundException("Fichier client_secret introuvable : $CLIENT_SECRET_FILE_PATH")}
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(FileInputStream(clientSecretFile)))
        val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build()
        val credential = flow.loadCredential("user")
        if (credential != null && credential.refreshToken != null) {return credential}
        val receiver = com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver.Builder().setPort(LOCAL_SERVER_PORT).build()
        try {gC.consoleMessage.value = ConsoleMessage("⏳ Ouverture du navigateur pour l'authentification Google Sheets...", ConsoleMessageType.INFO); return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")}
        catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de l'authentification Google Sheets : ${e.message}", ConsoleMessageType.ERROR);  throw e}
    }

    fun listAvailableSpreadsheets(): List<Pair<String, String>> {
        try {
            val driveService = Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials(GoogleNetHttpTransport.newTrustedTransport())).setApplicationName(APPLICATION_NAME).build()
            val result = driveService.files().list().setQ("mimeType='application/vnd.google-apps.spreadsheet'").setFields("files(id, name)").execute()
            return result.files.map {it.id to it.name}
        }
        catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("❌ Erreur lors de la récupération des feuilles : ${e.message}", ConsoleMessageType.ERROR); throw e}
    }

    fun createNewSpreadsheet(title: String): String {
        val service = getSheetsService()
        val spreadsheet = com.google.api.services.sheets.v4.model.Spreadsheet().setProperties(com.google.api.services.sheets.v4.model.SpreadsheetProperties().setTitle(title))
        val response = service.spreadsheets().create(spreadsheet).execute()
        val sheetId = response.spreadsheetId
        val headers = listOf(listOf("SOCIETE", "PRENOM", "NOM", "POSTE", "EMAIL", "TEL", "LINKEDIN"))
        val body = com.google.api.services.sheets.v4.model.ValueRange().setValues(headers)
        service.spreadsheets().values().update(sheetId, "A1", body).setValueInputOption("RAW").execute()
        return sheetId
    }
}