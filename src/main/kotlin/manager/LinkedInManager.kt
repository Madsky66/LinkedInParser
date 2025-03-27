package manager

import data.ProspectData
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.logging.Logger

class LinkedInManager {
    private val client = OkHttpClient()
    private val logger = Logger.getLogger(LinkedInManager::class.java.name)

    fun extractProfileData(text: String): ProspectData {
        logger.info("Début de l'extraction des données du profil")

        val lines = text.split("\n").map {it.trim()}.filter {it.isNotEmpty()}
        if (lines.isEmpty()) {
            logger.warning("Texte vide ou aucune donnée exploitable")
            return emptyProspectData()
        }

        val baseIndex = lines.indexOfFirst {it.contains("Image d’arrière-plan")}
        if (baseIndex == -1 || baseIndex + 5 >= lines.size) {
            logger.warning("Indexation incorrecte des lignes du profil")
            return emptyProspectData()
        }

        val firstNameLastNameLine = lines.getOrNull(baseIndex + 1) ?: ""
        val secondNameLine = lines.getOrNull(baseIndex + 2) ?: ""

        var company = lines.getOrNull(baseIndex + 4) ?: "Entreprise inconnue"

        if (firstNameLastNameLine == secondNameLine && firstNameLastNameLine.isNotEmpty()) {
            val fullName = firstNameLastNameLine.split(" ").filter {it.isNotEmpty()}
            val firstName = fullName.firstOrNull() ?: "Prénom inconnu"
            val lastName = fullName.lastOrNull() ?: "Nom de famille inconnu"
            val middleName = if (fullName.size > 2) fullName.subList(1, fullName.size - 1).joinToString(" ") else ""

            val apolloData = fetchApolloData(firstName, lastName, company)
            val personData = apolloData?.optJSONObject("person")

            val linkedInURL = personData?.optString("linkedin_url", null)
            val email = personData?.optString("email", null)
            company = personData?.optString("organization_name", null) ?: "Entreprise inconnue"
            val domain = extractDomain(company)
            val jobTitle = personData?.optString("headline", null) ?: "Poste inconnu"

            val emails = generateEmailVariations(firstName, lastName, domain).toMutableList()
            if (!email.isNullOrEmpty() && !emails.contains(email)) emails.add(email)

            logger.info("Emails générés : ${emails.joinToString(", ")}")

            return ProspectData(
                linkedinURL = linkedInURL.toString(),
                fullName = fullName.toString(),
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                email = emails.firstOrNull() ?: "",
                generatedEmails = emails,
                company = company,
                jobTitle = jobTitle
            )
        }
        logger.warning("Impossible de déterminer le nom et le prénom correctement")
        return emptyProspectData()
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
        val apiKey = "yvak05gEB4UywwXxmRedew"
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