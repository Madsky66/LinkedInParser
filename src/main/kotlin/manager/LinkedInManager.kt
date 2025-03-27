package manager

import data.ProspectData

class LinkedInManager {

    fun extractProfileData(text: String): ProspectData {
        val lines = text.split("\n")
        print("lines = $lines")

        val baseIndex = lines.indexOfFirst {it.contains("Image d’arrière-plan")} + 2

        val firstNameLastNameLine = lines[baseIndex].trim()
        val secondNameLine = lines[baseIndex + 2].trim()

        val jobTitle = lines[baseIndex + 3]
        val company = lines[baseIndex + 5].trim()

        if (firstNameLastNameLine == secondNameLine && firstNameLastNameLine.isNotEmpty()) {
            val names = firstNameLastNameLine.split(" ")
            val firstName = names.firstOrNull() ?: ""
            val lastName = names.lastOrNull() ?: ""
            val middleName = if (names.size > 2) names.subList(1, names.size - 1).joinToString(" ") else ""
            print("\n\nfirstName = $firstName, middleName = $middleName, lastName = $lastName\n\n")

            val emails = mutableListOf<String>()
            val domain = company.lowercase().replace(" ", "") + ".com"

            print("company = $company | domain = $domain")

            if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                emails.add("$firstName.$lastName@$domain")
                emails.add("$firstName@$domain")
                emails.add("$lastName@$domain")
                emails.add("${firstName.first()}${lastName}@$domain")
            }

            return ProspectData(
                fullName = firstNameLastNameLine,
                jobTitle = jobTitle,
                company = company,
                firstName = firstName,
                lastName = lastName,
                email = emails.firstOrNull() ?: "",
                generatedEmails = emails,
            )
        }

        return ProspectData(
            fullName = "Nom inconnu",
            jobTitle = jobTitle,
            company = company,
            firstName = "",
            lastName = "",
            email = "",
            generatedEmails = emptyList(),
        )
    }
}