package utils

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.util.Collections

object GoogleSheetsHelper {
    private const val APPLICATION_NAME = "LinkedInScraper"
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    private val SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS)

    fun getSheetsService(clientSecretPath: String): Sheets {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credentials = getCredentials(clientSecretPath, httpTransport)
        return Sheets.Builder(httpTransport, JSON_FACTORY, credentials).setApplicationName(APPLICATION_NAME).build()
    }

    private fun getCredentials(clientSecretPath: String, httpTransport: NetHttpTransport): Credential {
        val clientSecretFile = File(clientSecretPath)
        if (!clientSecretFile.exists()) {throw FileNotFoundException("Fichier client_secret introuvable : $clientSecretPath")}

        // Charger les informations d'identification client
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(FileInputStream(clientSecretFile)))

        // Configurer le flux d'autorisation
        val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setAccessType("offline").build()

        // Fournir un lien pour l'autorisation manuelle
        val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri("urn:ietf:wg:oauth:2.0:oob").build()
        println("Veuillez ouvrir le lien suivant dans votre navigateur :")
        println(authorizationUrl)

        // Demander à l'utilisateur de saisir le code d'autorisation
        print("Entrez le code d'autorisation : ")
        val code = readLine()

        // Échanger le code contre un jeton d'accès
        return flow.newTokenRequest(code).setRedirectUri("urn:ietf:wg:oauth:2.0:oob").execute().let {flow.createAndStoreCredential(it, "user")}
    }
}