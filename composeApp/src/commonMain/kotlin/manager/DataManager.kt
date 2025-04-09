package manager

import data.ProspectData

class DataManager {
    private val excludePatterns = listOf("otification", "contenu", "Profil", "echerche", "accourcis", "menu", "Accueil", "Réseau", "Emplois", "Messagerie", "Vous", "Pour les entreprises", "Premium", "Image", "relation", "Le statut est accessible", "clavier", "nouvelles", "actualité", "test", "Coordonnées", "Voir le profil complet", "Connexions", "Abonné", "Abonnés", "Voir tous les articles")
    private val linkedInUrlPattern = Regex("(https?://)?(www\\.)?linkedin\\.com/in/[\\w-]+(/)?")
    private val emailPattern = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val phoneNumberPattern = Regex("(0|\\+33|0033)[1-9]([-. ]?[0-9]{2}){4}")

    fun preprocessText(text: String): List<String> {return text.split("\n").map {it.trim()}.filter {it.isNotEmpty()}.filterNot {line -> excludePatterns.any {pattern -> line.contains(pattern, ignoreCase = true)}}}

    fun extractBasicData(lines: List<String>): ProspectData {
        val fullNameLine = lines.firstOrNull() ?: ""
        val fullNameParts = fullNameLine.split(" ").filter {it.isNotEmpty()}
        val firstName = fullNameParts.firstOrNull() ?: "Prénom inconnu"
        val middleName = if (fullNameParts.size > 2) fullNameParts.subList(1, fullNameParts.size - 1).joinToString(" ") else ""
        val lastName = if (fullNameParts.size > 1) fullNameParts.last() else "Nom de famille inconnu"
        val jobTitle = extractJobTitle(lines) ?: "Poste inconnu"
        val company = extractCompany(lines, jobTitle) ?: "Entreprise inconnue"
        val email = extractEmail(lines) ?: "Email inconnu"
        val phoneNumber = extractPhoneNumber(lines) ?: ""
        val linkedinUrl = extractLinkedInUrl(lines) ?: "Url inconnu"
        return ProspectData(company, "$firstName $lastName", firstName, middleName, lastName, jobTitle, email, phoneNumber, linkedinUrl)
    }
    private fun extractLinkedInUrl(lines: List<String>): String? {return lines.find {it.matches(linkedInUrlPattern)}}
    private fun extractJobTitle(lines: List<String>): String? {
        val jobTitleIndex = lines.indexOfFirst {it.contains("ExpérienceExpérience")}
        if (jobTitleIndex != -1 && jobTitleIndex + 1 < lines.size) {return lines[jobTitleIndex + 1]}
        val commonTitles = listOf("CEO", "CTO", "CFO", "COO", "Directeur", "Manager", "Ingénieur", "Développeur", "Consultant")
        return lines.find {line -> commonTitles.any {title -> line.contains(title, true)}}
    }
    private fun extractCompany(lines: List<String>, jobTitle: String): String? {
        val companyIndex = if (jobTitle != "" && jobTitle != "Poste Inconnu") {lines.indexOfFirst {it.contains(jobTitle)}} else {lines.indexOfFirst {it.contains("chez", true)}}
        if (companyIndex != -1 && companyIndex + 5 < lines.size) {
            var loopIterations = 1
            var tempCompany = lines[companyIndex + loopIterations]
            while ((loopIterations < 6) && (tempCompany == jobTitle)) {
                loopIterations ++
                tempCompany = lines[companyIndex + loopIterations]
            }
            if ((companyIndex >= 6) && (tempCompany == jobTitle)) {return null}
            return lines[companyIndex + 1]
        }
        return null
    }
    private fun extractEmail(lines: List<String>): String? {lines.forEach {line -> val match = emailPattern.find(line); if (match != null) {return match.value}}; return null}
    private fun extractPhoneNumber(lines: List<String>): String? {lines.forEach {line -> val match = phoneNumberPattern.find(line); if (match != null) {return match.value}}; return null}

    companion object {fun emptyProspectData(): ProspectData = ProspectData("Entreprise inconnue", "Nom inconnu", "Prénom inconnu", "", "Nom de famille inconnu", "Poste inconnu", "", "", "")}
}