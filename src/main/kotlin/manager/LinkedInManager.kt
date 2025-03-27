package manager

import data.ProspectData
import okhttp3.OkHttpClient
import okhttp3.Request

class LinkedInManager {

    fun extractProfileData(text: String): ProspectData {
        val lines = text.split("\n").map {it.trim()}.filter {it.isNotEmpty()}
        if (lines.isEmpty()) return emptyProspectData()
        print("lines = $lines")

        val baseIndex = lines.indexOfFirst {it.contains("Image d’arrière-plan")}
        if (baseIndex + 5 >= lines.size) return emptyProspectData()

        val firstNameLastNameLine = lines.getOrNull(baseIndex+1) ?: ""
        val secondNameLine = lines.getOrNull(baseIndex + 2) ?: ""

        val jobTitle = lines.getOrNull(baseIndex + 3) ?: "Poste inconnu"
        val company = lines.getOrNull(baseIndex + 4) ?: "Entreprise inconnue"

        if (firstNameLastNameLine == secondNameLine && firstNameLastNameLine.isNotEmpty()) {
            val names = firstNameLastNameLine.split(" ").filter {it.isNotEmpty()}
            val fullName = firstNameLastNameLine
            val firstName = names.firstOrNull() ?: "Prénom inconnu"
            val lastName = names.lastOrNull() ?: "Nom de famille inconnu"
            val middleName = if (names.size > 2) names.subList(1, names.size - 1).joinToString(" ") else ""
            print("\n\nfullName = $fullName\n\n\n\nfirstName = $firstName | middleName = $middleName | lastName = $lastName\n\n")
            val domain = extractDomain(company)

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("https://api.apollo.io/api/v1/people/match?reveal_personal_emails=false&reveal_phone_number=false")
                .post(null)
                .addHeader("accept", "application/json")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", "yvak05gEB4UywwXxmRedew")
                .build()

            val response = client.newCall(request).execute()

            val apolloEmail = response.body.toString().first().["email"].toString()

            val emails = generateEmailVariations(firstName, lastName, domain)
            if (apolloEmail != null) {emails.plus(apolloEmail)}

            print("company = $company | jobTitle = ${jobTitle} | domain = $domain")
            print("\n\nemails = $emails\n\n")

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

    private fun extractDomain(company: String): String {return company.lowercase().replace(Regex("[^a-z0-9]"), "").plus(".com")}

    private fun generateEmailVariations(firstName: String, lastName: String, domain: String): List<String> {
        if (firstName.isEmpty() || lastName.isEmpty()) return emptyList()
        return listOf(
            "${firstName.lowercase()}@${domain.lowercase()}",
            "${firstName.lowercase()}.${lastName.lowercase()}@${domain.lowercase()}",
            "${lastName.lowercase()}@${domain.lowercase()}",
            "${firstName.first().lowercase()}${lastName.lowercase()}@${domain.lowercase()}",
            "${firstName.lowercase()}-${{lastName.lowercase()}}@${domain.lowercase()}",
            "${firstName.lowercase()}${lastName.first().lowercase()}@${domain.lowercase()}",
            "${lastName.first().lowercase()}${firstName.lowercase()}@${domain.lowercase()}"
        )
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