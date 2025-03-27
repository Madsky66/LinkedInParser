package manager

import data.ProspectData

class LinkedInManager {

    fun extractProfileData(text: String): ProspectData {
        val nameRegex = """Image d’arrière-plan\s+([\w\s-]+)\s+\1""".toRegex()
        val titleRegex = """Entrepreneur innovant / Freelance - ([\w\s&-]+)""".toRegex()
        val locationRegex = """\n([\w\s, -]+)\s+Coordonnées""".toRegex()
        val experienceRegex = """ExpérienceExpérience\s+([\w\s&.-]+)\nMadsky · Freelance""".toRegex()

        val name = nameRegex.find(text)?.groupValues?.get(1) ?: "Nom inconnu"
        val title = titleRegex.find(text)?.groupValues?.get(1) ?: "Titre inconnu"
        val location = locationRegex.find(text)?.groupValues?.get(1) ?: "Localisation inconnue"
        val experience = experienceRegex.find(text)?.groupValues?.get(1) ?: "Expérience non trouvée"

        val emails = mutableListOf<String>()
        val domain = experience.lowercase().replace(" ", "") + ".com"

        if (name.isNotEmpty()) {
            val names = name.split(" ")
            val firstName = names.firstOrNull() ?: ""
            val lastName = names.lastOrNull() ?: ""
            emails.add("$firstName.$lastName@$domain")
            emails.add("$firstName@$domain")
            emails.add("$lastName@$domain")
            emails.add("${firstName.first()}${lastName}@$domain")
        }

        return ProspectData(
            fullName = name,
            position = title,
            company = experience,
            firstName = name.split(" ").firstOrNull() ?: "",
            lastName = name.split(" ").lastOrNull() ?: "",
            location = location,
            email = emails.firstOrNull() ?: "",
            generatedEmails = emails,
        )
    }
}