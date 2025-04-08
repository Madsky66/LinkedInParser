package utils

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
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
import java.awt.Desktop
import java.awt.Dialog
import java.awt.Frame
import java.net.URI

object GoogleSheetsHelper {
    private const val APPLICATION_NAME = "LinkedInScraper"
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE_METADATA_READONLY)
    private const val TOKENS_DIRECTORY_PATH = "tokens"
    private const val CLIENT_SECRET_FILE_PATH = "composeApp/src/jvmMain/composeResources/file/client_secret.json"

    fun getSheetsService(): Sheets {
        gC.consoleMessage.value = ConsoleMessage("--------------------------------------- getSheetsService", ConsoleMessageType.INFO)
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credentials = getCredentials(httpTransport)
        return Sheets.Builder(httpTransport, JSON_FACTORY, credentials).setApplicationName(APPLICATION_NAME).build()
    }

    fun openDialog(dialogTitle: String, isVisible: Boolean = true): String? {
        val fileDialog = Dialog(Frame(), dialogTitle)
        fileDialog.isVisible = isVisible
        return ""
    }

    private fun getCredentials(httpTransport: NetHttpTransport): Credential {
        val clientSecretFile = File(CLIENT_SECRET_FILE_PATH)
        if (!clientSecretFile.exists()) {throw FileNotFoundException("Fichier client_secret introuvable : $CLIENT_SECRET_FILE_PATH")}
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(FileInputStream(clientSecretFile)))
        val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build()
        val credential = flow.loadCredential("user")
        if (credential != null && credential.refreshToken != null) {return credential}
        val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri("urn:ietf:wg:oauth:2.0:oob").build()

        try {Desktop.getDesktop().browse(URI(authorizationUrl)); gC.consoleMessage.value = ConsoleMessage("✅ Navigateur ouvert pour l'authentification Google Sheets", utils.ConsoleMessageType.INFO)}
        catch (e: Exception) {gC.consoleMessage.value = ConsoleMessage("⚠️ Impossible d'ouvrir le navigateur automatiquement. Veuillez copier l'URL suivante : $authorizationUrl", utils.ConsoleMessageType.WARNING)}

        val authCode = openDialog("")
        return flow.newTokenRequest(authCode).setRedirectUri("urn:ietf:wg:oauth:2.0:oob").execute().let {flow.createAndStoreCredential(it, "user")}
    }

    fun listAvailableSpreadsheets(): List<Pair<String, String>> {
        val driveService = Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials(GoogleNetHttpTransport.newTrustedTransport())).setApplicationName(APPLICATION_NAME).build()
        val result = driveService.files().list().setQ("mimeType='application/vnd.google-apps.spreadsheet'").setFields("files(id, name)").execute()
        return result.files.map {it.id to it.name}
    }

    fun createNewSpreadsheet(title: String): String {
        val service = getSheetsService()
        val spreadsheet = com.google.api.services.sheets.v4.model.Spreadsheet().setProperties(com.google.api.services.sheets.v4.model.SpreadsheetProperties().setTitle(title))
        val response = service.spreadsheets().create(spreadsheet).execute()
        return response.spreadsheetId
    }
}