package manager

import data.ProspectData
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.logging.Logger

class LinkedInManager {
    private val client = OkHttpClient()
    private val logger = Logger.getLogger(LinkedInManager::class.java.name)

    suspend fun extractProfileData(text: String, apiKey: String = ""): ProspectData {
        logger.info("Début de l'extraction des données du profil")

        if (text.isBlank()) {
            logger.warning("Texte vide ou aucune donnée exploitable")
            return emptyProspectData()
        }

        return try {
            val lines = text.split("\n").map {it.trim()}.filter {it.isNotEmpty()}
                .filterNot {it.contains("Pour les entreprises")
                            || it.contains("Premium")
                            || it.contains("Image")
                            || it.contains("Le statut est accessible")
                            || it.contains("echerche")
                            || it.contains("otification")
                            || it.contains("accourcis")
                            || it.contains("clavier")
                            || it.contains("Fermer le menu de navigation")
                            || it.contains("nouvelles")
                            || it.contains("actualité")
                            || it.contains("Accueil")
                            || it.contains("Réseau")
                            || it.contains("contenu")
                            || it.contains("Emplois")
                            || it.contains("Messagerie")
                            || it.contains("Vous")
                            || it.contains("test")
                            || it.contains("relation")
                }

            // Extraction robuste du nom
            val fullName = lines.getOrNull(2)?.split(" ")?.filter {it.isNotEmpty()} ?: emptyList()
            val firstName = fullName.firstOrNull() ?: "Prénom inconnu"
            val middleName = if (fullName.size > 2) fullName.subList(1, fullName.size - 1).joinToString(" ") else ""
            val lastName = fullName.lastOrNull() ?: "Nom de famille inconnu"

            // Récupération de l'entreprise
            var company = (lines.getOrNull(5) ?: "Entreprise inconnue").toString()
            var jobTitle = (lines.getOrNull(4) ?: "Poste inconnu").toString()

            // Récupération des données Apollo
            val apolloData = fetchApolloData(firstName, lastName, company, apiKey)
            delay(500)
            val personData = apolloData?.optJSONObject("person")
            val linkedInURL = personData?.optString("linkedin_url", "URL introuvable").toString()

            // Récupération de l'entreprise et du poste
            if (company == "Entreprise inconnue") {
                val lastJobHistory = personData?.optJSONArray("employment_history")?.optJSONObject(0)
                val apolloCompany = lastJobHistory?.optString("organization_name", "Entreprise inconnue")?.takeIf {it.isNotBlank()}
                company = if (apolloCompany != "") {"Entreprise inconnue"} else {apolloCompany.toString()}
            }
            val domain = extractDomain(company)
            if (jobTitle == "Poste inconnu") {
                val lastJobHistory = personData?.optJSONArray("employment_history")?.optJSONObject(0)
                val apolloJobTitle = lastJobHistory?.optString("title", "Poste inconnu")?.takeIf {it.isNotBlank()}
                jobTitle = if (apolloJobTitle != "") {"Poste inconnu"} else {apolloJobTitle.toString()}
            }

            // Génération des emails
            val email = personData?.optString("email")?.takeIf {it.isNotBlank()}.toString()
            var generatedEmails = mutableListOf<String>()
            generatedEmails.add(email)
            generateEmailVariations(firstName, lastName, domain).toMutableList().forEach {email -> generatedEmails.add(email)}

            return ProspectData(
                linkedinURL = linkedInURL,
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

    private fun fetchApolloData(firstName: String, lastName: String, company: String, apiKey: String): JSONObject? {
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
            val requestBody = RequestBody.create("application/json".toMediaType(), jsonBody.toString())
            val request = Request.Builder()
                .url("https://api.apollo.io/api/v1/people/match")
                .post(requestBody)
                .addHeader("accept", "application/json")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", apiKey)
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