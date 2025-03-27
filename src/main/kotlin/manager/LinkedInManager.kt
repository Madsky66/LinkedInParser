package manager

import data.ProspectData

class LinkedInManager {

    fun extractProfileData(text: String): ProspectData {
        val titleRegex = """Entrepreneur innovant / Freelance - ([\w\s&-]+)""".toRegex()
        val locationRegex = """\n([\w\s, -]+)\s+Coordonnées""".toRegex()
        val experienceRegex = """ExpérienceExpérience\s+([\w\s&.-]+)\nMadsky · Freelance""".toRegex()

        val title = titleRegex.find(text)?.groupValues?.get(1) ?: "Titre inconnu"
        val location = locationRegex.find(text)?.groupValues?.get(1) ?: "Localisation inconnue"
        val experience = experienceRegex.find(text)?.groupValues?.get(1) ?: "Expérience non trouvée"

        val lines = text.split("\n")
        val nameLineIndex = lines.indexOfFirst {it.contains("Image d’arrière-plan")} + 1

        print("text = $text")
        print("lines = $lines")
        print("nameLineIndex = $nameLineIndex")

        val firstNameLastNameLine = if (nameLineIndex < lines.size) lines[nameLineIndex].trim() else ""
        val secondNameLine = if (nameLineIndex + 1 < lines.size) lines[nameLineIndex + 1].trim() else ""

        print("firstNameLastNameLine = $firstNameLastNameLine")
        print("secondNameLine = $secondNameLine")

        // Vérifier si les deux lignes sont identiques
        if (firstNameLastNameLine == secondNameLine) {
            val names = firstNameLastNameLine.split(" ")
            val firstName = names.firstOrNull() ?: ""
            val lastName = names.lastOrNull() ?: ""
            val middleName = if (names.size > 2) names.subList(1, names.size - 1).joinToString(" ") else ""

            val emails = mutableListOf<String>()
            val domain = experience.lowercase().replace(" ", "") + ".com"

            if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                emails.add("$firstName.$lastName@$domain")
                emails.add("$firstName@$domain")
                emails.add("$lastName@$domain")
                emails.add("${firstName.first()}${lastName}@$domain")
            }

            return ProspectData(
                fullName = firstNameLastNameLine,
                position = title,
                company = experience,
                firstName = firstName,
                lastName = lastName,
                location = location,
                email = emails.firstOrNull() ?: "",
                generatedEmails = emails,
            )
        }

        return ProspectData(
            fullName = "Nom inconnu",
            position = title,
            company = experience,
            firstName = "",
            lastName = "",
            location = location,
            email = "",
            generatedEmails = emptyList(),
        )
    }
}