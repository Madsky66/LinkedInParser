package manager

import config.GlobalInstance.config as gC
import data.ProspectData
import kotlinx.coroutines.*
import manager.DataManager.Companion.emptyProspectData
import utils.*

class LinkedInManager {
    private val dataManager = DataManager()
    private val apolloManager = ApolloManager()
    private val generationManager = GenerationManager()

    fun processInput(applicationScope: CoroutineScope, input: String) {
        applicationScope.launch {
            gC.isExtractionLoading.value = true
            when {
                input.isBlank() -> gC.consoleMessage.value = ConsoleMessage("En attente de données...", ConsoleMessageType.INFO)
                input.length < 5000 -> gC.consoleMessage.value = ConsoleMessage("⚠️ Trop peu de texte, veuillez vérifier l'URL de la page (\"http(s)://(www.)linkedin.com/in/...\") ou le texte copié", ConsoleMessageType.WARNING)
                else -> {
                    gC.consoleMessage.value = ConsoleMessage("⏳ Extraction des informations en cours...", ConsoleMessageType.INFO)
                    val newProfile = extractProfileData(input)
                    gC.currentProfile.value = newProfile
                    gC.consoleMessage.value = when {
                        newProfile.fullName.isBlank() || (newProfile.firstName == "Prénom inconnu" && newProfile.lastName == "Nom de famille inconnu") -> ConsoleMessage("❌ Aucune information traitable ou format du texte copié incorrect", ConsoleMessageType.ERROR)
                        newProfile.firstName == "Prénom inconnu" || newProfile.lastName == "Nom de famille inconnu" -> ConsoleMessage("⚠️ Extraction des données incomplète", ConsoleMessageType.WARNING)
                        else -> ConsoleMessage("✅ Extraction des informations réussie", ConsoleMessageType.SUCCESS)
                    }
                }
            }
            gC.isExtractionLoading.value = false
        }
    }

    fun extractProfileData(text: String): ProspectData {
        if (text.isBlank()) return emptyProspectData()
        return try {
            // Extraction des données de base
            val lines = dataManager.preprocessText(text)
            val basicData = dataManager.extractBasicData(lines)
            // Enrichissement via Apollo si possible
            val enrichedData = apolloManager.enrichProfileData(basicData.firstName, basicData.lastName, basicData.company, basicData.jobTitle, basicData.email)
            // Génération d'emails suggérés
            val domain = generationManager.extractDomain(enrichedData.company)
            val generatedEmails = generationManager.generateEmails(enrichedData.firstName, enrichedData.middleName, enrichedData.lastName, domain, enrichedData.company).distinct().toMutableList()
            // Ajout de l'email connu s'il existe et n'est pas déjà dans la liste
            if (enrichedData.email != "Email inconnu" && !generatedEmails.contains(enrichedData.email)) {generatedEmails.add(0, enrichedData.email)}
            // Création du profil final
            enrichedData.copy(generatedEmails = generatedEmails, email = if (enrichedData.email == "Email inconnu" && generatedEmails.isNotEmpty()) generatedEmails.first() else enrichedData.email)
        }
        catch (e: Exception) {e.printStackTrace(); emptyProspectData()}
    }
}