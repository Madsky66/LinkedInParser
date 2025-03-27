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
        val lines = text.split("\n").map {it.trim()}.filter {it.isNotEmpty()}
        if (lines.isEmpty()) return emptyProspectData()

        val baseIndex = lines.indexOfFirst {it.contains("Image d’arrière-plan")}
        if (baseIndex == -1 || baseIndex + 5 >= lines.size) return emptyProspectData()

        val firstNameLastNameLine = lines.getOrNull(baseIndex + 1) ?: ""
        val secondNameLine = lines.getOrNull(baseIndex + 2) ?: ""

        val jobTitle = lines.getOrNull(baseIndex + 3) ?: "Poste inconnu"
        val company = lines.getOrNull(baseIndex + 4) ?: "Entreprise inconnue"

        if (firstNameLastNameLine == secondNameLine && firstNameLastNameLine.isNotEmpty()) {
            val names = firstNameLastNameLine.split(" ").filter {it.isNotEmpty()}
            val fullName = firstNameLastNameLine
            val firstName = names.firstOrNull() ?: "Prénom inconnu"
            val lastName = names.lastOrNull() ?: "Nom de famille inconnu"
            val middleName = if (names.size > 2) names.subList(1, names.size - 1).joinToString(" ") else ""

            val domain = extractDomain(company)
            val apolloEmail = fetchApolloEmail(firstName, lastName, company)

            logger.info("apolloEmail = $apolloEmail")

            val emails = generateEmailVariations(firstName, lastName, domain).toMutableList()
            if (!apolloEmail.isNullOrEmpty()) emails.add(apolloEmail)

            return ProspectData(
                fullName = fullName,
                firstName = firstName,
                lastName = lastName,
                email = emails.firstOrNull() ?: "",
                generatedEmails = emails,
                company = company,
                jobTitle = jobTitle
            )
        }

        return emptyProspectData()
    }

    private fun extractDomain(company: String): String {
        if (company.isBlank()) return "domaine_inconnu.com"
        return company.lowercase().replace(Regex("[^a-z0-9]"), "") + ".com"
    }

    private fun generateEmailVariations(firstName: String, lastName: String, domain: String): List<String> {
        if (firstName.isBlank() || lastName.isBlank() || domain.isBlank()) return emptyList()

        val cleanFirstName = firstName.lowercase().replace(Regex("[^a-z]"), "")
        val cleanLastName = lastName.lowercase().replace(Regex("[^a-z]"), "")
        val cleanDomain = domain.lowercase()

        return listOf(
            "$cleanFirstName@$cleanDomain",
            "$cleanFirstName.$cleanLastName@$cleanDomain",
            "$cleanFirstName-$cleanLastName@$cleanDomain",
            "$cleanLastName@$cleanDomain",
            "${cleanFirstName.first()}$cleanLastName@$cleanDomain",
            "$cleanFirstName${cleanLastName.first()}@$cleanDomain",
            "${cleanLastName.first()}$cleanFirstName@$cleanDomain",
            "${cleanFirstName}_${cleanLastName}@$cleanDomain",
            "$cleanFirstName$cleanLastName@$cleanDomain",
            "$cleanLastName.$cleanFirstName@$cleanDomain",
            "${cleanFirstName.take(3)}${cleanLastName.take(3)}@$cleanDomain"
        ).distinct()
    }

    private fun fetchApolloEmail(firstName: String, lastName: String, company: String): String? {
        if (firstName.isBlank() || lastName.isBlank() || company.isBlank()) return null
        val apiKey = "yvak05gEB4UywwXxmRedew"
        val jsonBody = JSONObject().apply {
            put("first_name", firstName)
            put("last_name", lastName)
            put("organization_name", company)
            put("reveal_personal_emails", false)
            put("reveal_phone_number", false)
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
                    logger.warning("Échec de la récupération de l'email Apollo: ${response.code}")
                    return null
                }
                val responseBody = response.body?.string() ?: return null

                logger.info("responseBody = $responseBody")

                val jsonResponse = JSONObject(responseBody)
                jsonResponse.optString("email", null)
            }
        }
        catch (e: Exception) {
            logger.severe("Erreur lors de la récupération de l'email Apollo: ${e.message}")
            null
        }
    }

    private fun emptyProspectData(): ProspectData {
        return ProspectData(
            fullName = "Nom inconnu",
            firstName = "Prénom inconnu",
            lastName = "Nom de famille inconnu",
            email = "",
            generatedEmails = emptyList(),
            company = "Entreprise inconnue",
            jobTitle = "Poste inconnu"
        )
    }
}