package manager

import data.ProspectData

class DataManager {
    private val excludePatterns = listOf("otification", "contenu", "Profil", "echerche", "accourcis", "menu", "Accueil", "Réseau", "Emplois", "Messagerie", "Vous", "Pour les entreprises", "Premium", "Image", "relation", "Le statut est accessible", "clavier", "nouvelles", "actualité", "test", "Coordonnées", "Voir le profil complet", "Connexions", "Abonné", "Abonnés", "Voir tous les articles")
    private val linkedInUrlPattern = Regex("(https?://)?(www\\.)?linkedin\\.com/in/[\\w-]+(/)?")
    private val emailPattern = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")

    fun preprocessText(text: String): List<String> {return text.split("\n").map {it.trim()}.filter {it.isNotEmpty()}.filterNot {line -> excludePatterns.any { pattern -> line.contains(pattern, ignoreCase = true) }}}
    fun extractBasicData(lines: List<String>): ProspectData {
        val linkedinUrl = extractLinkedInUrl(lines) ?: "Url inconnu"
        val fullNameLine = lines.firstOrNull() ?: ""
        val fullNameParts = fullNameLine.split(" ").filter { it.isNotEmpty() }
        val firstName = fullNameParts.firstOrNull() ?: "Prénom inconnu"
        val middleName = if (fullNameParts.size > 2) fullNameParts.subList(1, fullNameParts.size - 1).joinToString(" ") else ""
        val lastName = if (fullNameParts.size > 1) fullNameParts.last() else "Nom de famille inconnu"
        val email = extractEmail(lines) ?: "Email inconnu"
        val jobTitle = extractJobTitle(lines) ?: "Poste inconnu"
        val company = extractCompany(lines) ?: "Entreprise inconnue"
        return ProspectData(linkedinUrl, "$firstName $lastName", firstName, middleName, lastName, email, emptyList(), company, jobTitle)
    }
    private fun extractLinkedInUrl(lines: List<String>): String? {return lines.find {it.matches(linkedInUrlPattern)}}
    private fun extractJobTitle(lines: List<String>): String? {
        val jobTitleIndex = lines.indexOfFirst { it.contains("ExpérienceExpérience") }
        if (jobTitleIndex != -1 && jobTitleIndex + 1 < lines.size) {return lines[jobTitleIndex + 1]}
        val commonTitles = listOf("CEO", "CTO", "CFO", "COO", "Directeur", "Manager", "Ingénieur", "Développeur", "Consultant")
        return lines.find {line -> commonTitles.any { title -> line.contains(title, ignoreCase = true) }}
    }
    private fun extractCompany(lines: List<String>): String? {
        val companyIndex = lines.indexOfFirst { it.contains("chez") }
        if (companyIndex != -1 && companyIndex + 1 < lines.size) {return lines[companyIndex + 1]}
        val jobTitleIndex = lines.indexOfFirst { it.contains("Expérience", ignoreCase = true) }
        if (jobTitleIndex != -1 && jobTitleIndex + 2 < lines.size) {return lines[jobTitleIndex + 2]}
        return null
    }
    private fun extractEmail(lines: List<String>): String? {
        lines.forEach {line -> val match = emailPattern.find(line); if (match != null) {return match.value}}
        val emailIndex = lines.indexOf("E-mail")
        if (emailIndex != -1 && emailIndex + 1 < lines.size) {
            val nextLine = lines[emailIndex + 1]
            val match = emailPattern.find(nextLine)
            if (match != null) {return match.value}
            return nextLine
        }
        return null
    }

    companion object {fun emptyProspectData(): ProspectData = ProspectData("", "Nom inconnu", "Prénom inconnu", "", "Nom de famille inconnu", "", emptyList(), "Entreprise inconnue", "Poste inconnu")}
}