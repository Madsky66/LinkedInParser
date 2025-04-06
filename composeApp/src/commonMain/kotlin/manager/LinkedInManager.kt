package manager

import config.GlobalInstance.config as gC
import data.ProspectData
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import utils.*
import java.util.logging.Logger

class LinkedInManager {
    private val client = OkHttpClient()
    private val logger = Logger.getLogger(LinkedInManager::class.java.name)

    fun processInput(applicationScope: CoroutineScope, input: String) {
        val linkedinManager = LinkedInManager()
        applicationScope.launch {
            gC.isExtractionLoading.value = true
            when {
                input.isBlank() -> gC.consoleMessage.value = ConsoleMessage("En attente de données...", ConsoleMessageType.INFO)
                input.length < 5000 -> gC.consoleMessage.value = (ConsoleMessage("⚠️ Trop peu de texte, veuillez vérifier l'URL de la page (\"http(s)://(www.)linkedin.com/in/...\") ou le texte copié", ConsoleMessageType.WARNING))
                else ->  {
                    gC.isExtractionLoading.value = true
                    gC.consoleMessage.value = (ConsoleMessage("⏳ Extraction des informations en cours...", ConsoleMessageType.INFO))
                    gC.currentProfile.value = linkedinManager.extractProfileData(input)
                    val newProfile = linkedinManager.extractProfileData(input)
                    gC.consoleMessage.value =
                        (when {
                            newProfile.fullName.isBlank() || (newProfile.firstName == "Prénom inconnu" && newProfile.lastName == "Nom de famille inconnu") -> ConsoleMessage("❌ Aucune information traitable ou format du texte copié incorrect", ConsoleMessageType.ERROR)
                            newProfile.firstName == "Prénom inconnu" || newProfile.lastName == "Nom de famille inconnu" -> ConsoleMessage("⚠️ Extraction des données incomplète", ConsoleMessageType.WARNING)
                            else -> {ConsoleMessage("✅ Extraction des informations réussie", ConsoleMessageType.SUCCESS)}
                        })
                    gC.isExtractionLoading.value = false
                }
            }
            gC.isExtractionLoading.value = false
        }
    }

    suspend fun extractProfileData(text: String): ProspectData {
        logger.info("Début de l'extraction des données du profil")

        if (text.isBlank()) {
            logger.warning("Texte vide ou aucune donnée exploitable")
            return emptyProspectData()
        }

        return try {
            // Filtrage des données
            val lines = text.split("\n").map {it.trim()}.filter {it.isNotEmpty()}
                .filterNot {it.contains("otification")
                        || it.contains("contenu")
                        || it.contains("Profil")
                        || it.contains("echerche")
                        || it.contains("accourcis")
                        || it.contains("menu")
                        || it.contains("Accueil")
                        || it.contains("Réseau")
                        || it.contains("Emplois")
                        || it.contains("Messagerie")
                        || it.contains("Vous")
                        || it.contains("Pour les entreprises")
                        || it.contains("Premium")
                        || it.contains("Image")
                        || it.contains("relation")

                        || it.contains("Le statut est accessible")
                        || it.contains("clavier")
                        || it.contains("nouvelles")
                        || it.contains("actualité")
                        || it.contains("test")
                        || it.contains("Coordonnées")
                }

            // Extraction des données
            var linkedinUrl  = lines.getOrNull(1) ?: "Url inconnu"
            val fullName = lines.getOrNull(0)?.split(" ")?.filter {it.isNotEmpty()} ?: emptyList()
            val firstName = fullName.firstOrNull() ?: "Prénom inconnu"
            val middleName = if (fullName.size > 2) fullName.subList(1, fullName.size - 1).joinToString(" ") else ""
            val lastName = fullName.lastOrNull() ?: "Nom de famille inconnu"
            val emailIndex = lines.indexOf("E-mail")
            var email = if (emailIndex != -1 && emailIndex + 1 < lines.size) {lines.getOrNull(emailIndex + 1) ?: "Email inconnu"} else "Email inconnu"
            val jobTitleIndex = lines.indexOf("ExpérienceExpérience")
            var jobTitle = if (jobTitleIndex != -1 && jobTitleIndex + 1 < lines.size) {lines.getOrNull(jobTitleIndex + 1) ?: "Poste inconnu"} else {"Poste inconnu"}
            val companyIndex = lines.indexOf("$fullName\n$fullName\n$jobTitle")
            var company = if (companyIndex != -1 && companyIndex + 4 < lines.size) {lines.getOrNull(companyIndex + 4) ?: "Entreprise inconnue"} else {"Entreprise inconnue"}

            // Enrichissement Apollo
            val apolloData = fetchApolloData(firstName, lastName, company)
            delay(500)
            val personData = apolloData?.optJSONObject("person")
            if (linkedinUrl == "Url inconnu") {linkedinUrl = personData?.optString("linkedin_url", "URL inconnu") ?: "Url inconnu"}
            if (company == "Entreprise inconnue") {
                val lastJobHistory = personData?.optJSONArray("employment_history")?.optJSONObject(0)
                company = lastJobHistory?.optString("organization_name", "Entreprise inconnue") ?: "Entreprise inconnue"
            }
            val domain = extractDomain(company)
            if (jobTitle == "Poste inconnu") {
                val lastJobHistory = personData?.optJSONArray("employment_history")?.optJSONObject(0)
                jobTitle = lastJobHistory?.optString("title", "Poste inconnu") ?: "Poste inconnu"
            }
            if (email == "Email inconnu") {email = personData?.optString("email")?.takeIf {it.isNotBlank()}.toString()}

            // Génération des emails
            var generatedEmails = mutableListOf<String>()
            generatedEmails.add(email)
            generateEmailVariations(firstName, lastName, domain).toMutableList().forEach {email -> generatedEmails.add(email)}

            return ProspectData(
                linkedinUrl = linkedinUrl,
                fullName = "$firstName $lastName",
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                email = email,
                generatedEmails = generatedEmails,
                company = company,
                jobTitle = jobTitle
            )
        }
        catch (e: Exception) {emptyProspectData()}
    }

    private fun extractDomain(company: String): String {
        return if (company.isBlank()) "domaine_inconnu.com"
        else "${company.lowercase().replace(Regex("[^a-z0-9]"), "")}.com"
    }

    private fun generateEmailVariations(firstName: String, lastName: String, domain: String): List<String> {
        if (firstName.isBlank() || lastName.isBlank() || domain.isBlank()) return emptyList()
        val cleanFirstName = firstName.lowercase().replace(Regex("[^a-z]"), "")
        val cleanLastName = lastName.lowercase().replace(Regex("[^a-z]"), "")
        return listOf(
            "$cleanFirstName@$domain",
            "$cleanFirstName.$cleanLastName@$domain",
            "$cleanFirstName-$cleanLastName@$domain",
            "$cleanLastName@$domain",
            "${cleanFirstName.first()}$cleanLastName@$domain",
            "$cleanFirstName${cleanLastName.first()}@$domain",
            "${cleanLastName.first()}$cleanFirstName@$domain",
            "${cleanFirstName}_${cleanLastName}@$domain",
            "$cleanFirstName$cleanLastName@$domain",
            "$cleanLastName.$cleanFirstName@$domain",
            "${cleanFirstName.take(3)}${cleanLastName.take(3)}@$domain"
        ).distinct()
    }

    private fun fetchApolloData(firstName: String, lastName: String, company: String): JSONObject? {
        if (firstName.isBlank() || lastName.isBlank()) {
            logger.warning("Données incomplètes pour interroger Apollo")
            return null
        }
        val jsonBody = JSONObject().apply {
            put("first_name", firstName)
            put("last_name", lastName)
            put("organization_name", company)
            put("reveal_personal_emails", true)
        }
        return try {
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://api.apollo.io/api/v1/people/match")
                .post(requestBody)
                .addHeader("accept", "application/json")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", gC.apiKey.value)
                .build()
            client.newCall(request).execute().use {response ->
                if (!response.isSuccessful) {
                    logger.warning("Échec de la récupération des données Apollo: ${response.code}")
                    return null
                }
                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    logger.warning("Réponse Apollo vide")
                    return null
                }
                logger.info("Réponse Apollo : $responseBody")
                return JSONObject(responseBody)
            }
        }
        catch (e: Exception) {
            logger.severe("Erreur Apollo : ${e.message}")
            null
        }
    }

    private fun emptyProspectData(): ProspectData {
        return ProspectData(
            fullName = "Nom inconnu",
            firstName = "Prénom inconnu",
            middleName = "",
            lastName = "Nom de famille inconnu",
            email = "",
            generatedEmails = emptyList(),
            company = "Entreprise inconnue",
            jobTitle = "Poste inconnu"
        )
    }
}