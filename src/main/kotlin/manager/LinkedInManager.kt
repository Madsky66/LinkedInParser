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


        return ProspectData(
            fullName = name,
            position = title,
            company = (experience.firstOrNull() ?: "").toString(),
            firstName = name.split(" ").firstOrNull() ?: "",
            lastName = name.split(" ").lastOrNull() ?: "",
            location = location,
        )
    }

}