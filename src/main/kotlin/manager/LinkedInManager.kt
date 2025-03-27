package manager

import data.ProspectData

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
            val firstName = names.firstOrNull() ?: ""
            val lastName = names.lastOrNull() ?: ""
            val middleName = if (names.size > 2) names.subList(1, names.size - 1).joinToString(" ") else ""
            print("\n\nfirstName = $firstName | middleName = $middleName | lastName = $lastName\n\n")

            val domain = extractDomain(company)

            val emails = generateEmailVariations(firstName, lastName, domain)

            print("company = $company | jobTitle = ${jobTitle} | domain = $domain")

            return ProspectData(
                fullName = firstNameLastNameLine,
                jobTitle = jobTitle,
                company = company,
                firstName = firstName,
                lastName = lastName,
                email = emails.firstOrNull() ?: "",
                generatedEmails = emails
            )
        }

        return emptyProspectData()
    }

    private fun extractDomain(company: String): String {return company.lowercase().replace(Regex("[^a-z0-9]"), "").plus(".com")}

    private fun generateEmailVariations(firstName: String, lastName: String, domain: String): List<String> {
        if (firstName.isEmpty() || lastName.isEmpty()) return emptyList()
        return listOf(
            "$firstName.$lastName@$domain",
            "$firstName@$domain",
            "$lastName@$domain",
            "${firstName.first()}$lastName@$domain",
            "$firstName-${lastName}@${domain}",
            "$firstName${lastName.first()}@$domain",
            "${lastName.first()}$firstName@$domain"
        )
    }

    private fun emptyProspectData(): ProspectData {
        return ProspectData(
            fullName = "Nom inconnu",
            jobTitle = "Poste inconnu",
            company = "Entreprise inconnue",
            firstName = "",
            lastName = "",
            email = "",
            generatedEmails = emptyList()
        )
    }
}