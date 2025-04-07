package manager

import config.GlobalInstance.config as gC
import data.ProspectData
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import utils.*
import kotlin.random.Random

class LinkedInManager {
    private val client = OkHttpClient()
    private val userAgents = listOf(
        // Chrome sur Windows
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36",

        // Firefox sur Windows
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:92.0) Gecko/20100101 Firefox/92.0",

        // Edge sur Windows
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36 Edg/92.0.902.78",

        // Safari sur macOS
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Safari/605.1.15",

        // Chrome sur macOS
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36",

        // Firefox sur macOS
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:89.0) Gecko/20100101 Firefox/89.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:90.0) Gecko/20100101 Firefox/90.0",

        // Chrome sur Android
        "Mozilla/5.0 (Linux; Android 11; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Mobile Safari/537.36",

        // Safari sur iOS
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1"
    )

    private fun getSmartUserAgent(): String {
        val browserDistribution = mapOf("Chrome" to 65, "Firefox" to 10, "Safari" to 15, "Edge" to 8, "Other" to 2)
        val randomValue = Random.nextInt(100)
        var cumulativePercentage = 0
        val selectedBrowser = browserDistribution.entries.find {(_, percentage) -> cumulativePercentage += percentage; randomValue < cumulativePercentage}?.key ?: "Chrome"
        return when (selectedBrowser) {
            "Chrome" -> userAgents.filter {it.contains("Chrome") && !it.contains("Edg")}.random()
            "Firefox" -> userAgents.filter {it.contains("Firefox")}.random()
            "Safari" -> userAgents.filter {it.contains("Safari") && !it.contains("Chrome")}.random()
            "Edge" -> userAgents.filter {it.contains("Edg")}.random()
            else -> userAgents.random()
        }
    }

    fun processInput(applicationScope: CoroutineScope, input: String) {
        applicationScope.launch {
            gC.isExtractionLoading.value = true
            when {
                input.isBlank() -> gC.consoleMessage.value = ConsoleMessage("En attente de données...", ConsoleMessageType.INFO)
                input.length < 5000 -> gC.consoleMessage.value = ConsoleMessage("⚠️ Trop peu de texte, veuillez vérifier l'URL de la page (\"http(s)://(www.)linkedin.com/in/...\") ou le texte copié", ConsoleMessageType.WARNING)
                else -> {
                    gC.consoleMessage.value = ConsoleMessage("⏳ Extraction des informations en cours...", ConsoleMessageType.INFO)
                    val newProfile = extractProfileData(input)
                    gC.currentProfile.value = newProfile
                    gC.consoleMessage.value = when {
                        newProfile.fullName.isBlank() || (newProfile.firstName == "Prénom inconnu" && newProfile.lastName == "Nom de famille inconnu") -> ConsoleMessage("❌ Aucune information traitable ou format du texte copié incorrect", ConsoleMessageType.ERROR)
                        newProfile.firstName == "Prénom inconnu" || newProfile.lastName == "Nom de famille inconnu" -> ConsoleMessage("⚠️ Extraction des données incomplète", ConsoleMessageType.WARNING)
                        else -> ConsoleMessage("✅ Extraction des informations réussie", ConsoleMessageType.SUCCESS)
                    }
                }
            }
            gC.isExtractionLoading.value = false
        }
    }

    fun extractProfileData(text: String): ProspectData {
        if (text.isBlank()) {return emptyProspectData()}
        return try {
            val excludePatterns = listOf("otification", "contenu", "Profil", "echerche", "accourcis", "menu", "Accueil", "Réseau", "Emplois", "Messagerie", "Vous", "Pour les entreprises", "Premium", "Image", "relation", "Le statut est accessible", "clavier", "nouvelles", "actualité", "test", "Coordonnées", "Voir le profil complet", "Connexions", "Abonné", "Abonnés", "Voir tous les articles")
            val lines = text.split("\n").map {it.trim()}.filter {it.isNotEmpty()}.filterNot {line -> excludePatterns.any {pattern -> line.contains(pattern, ignoreCase = true)}}

            // Extraction des données
            val linkedinUrl = extractLinkedInUrl(lines) ?: "Url inconnu"
            val fullNameLine = lines.firstOrNull() ?: ""
            val fullNameParts = fullNameLine.split(" ").filter {it.isNotEmpty()}
            val firstName = fullNameParts.firstOrNull() ?: "Prénom inconnu"
            val middleName = if (fullNameParts.size > 2) fullNameParts.subList(1, fullNameParts.size - 1).joinToString(" ") else ""
            val lastName = if (fullNameParts.size > 1) fullNameParts.last() else "Nom de famille inconnu"
            var email = extractEmail(lines) ?: "Email inconnu"
            var jobTitle = extractJobTitle(lines) ?: "Poste inconnu"
            var company = extractCompany(lines) ?: "Entreprise inconnue"

            // Enrichissement Apollo
            val apolloData = fetchApolloData(firstName, lastName, company)
            val personData = apolloData?.optJSONObject("person")
            if (personData != null) {
                if (company == "Entreprise inconnue") {
                    val lastJobHistory = personData.optJSONArray("employment_history")?.optJSONObject(0)
                    val apolloCompany = lastJobHistory?.optString("organization_name")
                    if (!apolloCompany.isNullOrBlank()) {company = apolloCompany}
                }
                if (jobTitle == "Poste inconnu") {
                    val lastJobHistory = personData.optJSONArray("employment_history")?.optJSONObject(0)
                    val apolloJobTitle = lastJobHistory?.optString("title")
                    if (!apolloJobTitle.isNullOrBlank()) {jobTitle = apolloJobTitle}
                }
                if (email == "Email inconnu") {
                    val apolloEmail = personData.optString("email")
                    if (apolloEmail.isNotBlank()) {email = apolloEmail}
                }
            }
            val domain = extractDomain(company)
            val generatedEmails = generateFrenchStyleEmails(firstName, middleName, lastName, domain, company).distinct().toMutableList()
            if (email != "Email inconnu" && !generatedEmails.contains(email)) {generatedEmails.add(0, email)}
            return ProspectData(linkedinUrl, "$firstName $lastName", firstName, middleName, lastName, if (email == "Email inconnu" && generatedEmails.isNotEmpty()) generatedEmails.first() else email, generatedEmails, company, jobTitle)
        }
        catch (e: Exception) {e.printStackTrace(); emptyProspectData()}
    }

    private fun extractLinkedInUrl(lines: List<String>): String? {
        val linkedInPattern = Regex("(https?://)?(www\\.)?linkedin\\.com/in/[\\w-]+(/)?")
        return lines.find {it.matches(linkedInPattern)}
    }

    private fun extractJobTitle(lines: List<String>): String? {
        val jobTitleIndex = lines.indexOfFirst {it.contains("ExpérienceExpérience")}
        if (jobTitleIndex != -1 && jobTitleIndex + 1 < lines.size) {return lines[jobTitleIndex + 1]}
        val commonTitles = listOf("CEO", "CTO", "CFO", "COO", "Directeur", "Manager", "Ingénieur", "Développeur", "Consultant")
        return lines.find {line -> commonTitles.any {title -> line.contains(title, ignoreCase = true)}}
    }

    private fun extractCompany(lines: List<String>): String? {
        val companyIndex = lines.indexOfFirst {it.contains("chez")}
        if (companyIndex != -1 && companyIndex + 1 < lines.size) {return lines[companyIndex + 1]}
        val jobTitleIndex = lines.indexOfFirst {it.contains("Expérience", ignoreCase = true)}
        if (jobTitleIndex != -1 && jobTitleIndex + 2 < lines.size) {return lines[jobTitleIndex + 2]}
        return null
    }

    private fun extractEmail(lines: List<String>): String? {
        val emailPattern = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
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

    private fun fetchApolloData(firstName: String, lastName: String, company: String): JSONObject? {
        if (firstName.isBlank() || lastName.isBlank()) {return null}
        try {
            val jsonBody = JSONObject().apply {
                put("first_name", firstName)
                put("last_name", lastName)
                put("organization_name", company)
                put("reveal_personal_emails", true)
            }
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url("https://api.apollo.io/api/v1/people/match").post(requestBody).addHeader("accept", "application/json").addHeader("Cache-Control", "no-cache").addHeader("Content-Type", "application/json").addHeader("User-Agent", getSmartUserAgent()).addHeader("x-api-key", gC.apiKey.value).build()
            client.newCall(request).execute().use {response ->
                if (!response.isSuccessful) {return null}
                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {return null}
                return JSONObject(responseBody)
            }
        }
        catch (e: Exception) {return null}
    }

    private fun extractDomain(company: String): String {
        if (company.isBlank()) return "domaine_inconnu.com"
        val cleanCompany = company.lowercase().replace(Regex("[^a-z0-9]"), "").replace(Regex("(inc|llc|ltd|sarl|sas|sa|eurl|group|groupe)$"), "")
        return "$cleanCompany.com"
    }

    private fun generateFrenchStyleEmails(firstName: String, middleName: String, lastName: String, domain: String, company: String): List<String> {
        if (firstName.isBlank() || lastName.isBlank() || domain.isBlank()) return emptyList()
        val cleanFirstName = firstName.lowercase().replace(Regex("[éèêë]"), "e").replace(Regex("[àâä]"), "a").replace(Regex("[ùûü]"), "u").replace(Regex("[îï]"), "i").replace(Regex("[ôö]"), "o").replace(Regex("[ç]"), "c").replace(Regex("[^a-z]"), "")
        val cleanMiddleName = middleName.lowercase().replace(Regex("[éèêë]"), "e").replace(Regex("[àâä]"), "a").replace(Regex("[ùûü]"), "u").replace(Regex("[îï]"), "i").replace(Regex("[ôö]"), "o").replace(Regex("[ç]"), "c").replace(Regex("[^a-z]"), "")
        val cleanLastName = lastName.lowercase().replace(Regex("[éèêë]"), "e").replace(Regex("[àâä]"), "a").replace(Regex("[ùûü]"), "u").replace(Regex("[îï]"), "i").replace(Regex("[ôö]"), "o").replace(Regex("[ç]"), "c").replace(Regex("[^a-z]"), "")
        val firstInitial = if (cleanFirstName.isNotEmpty()) cleanFirstName.first().toString() else ""
        val lastInitial = if (cleanLastName.isNotEmpty()) cleanLastName.first().toString() else ""
        val frenchEmailFormats = mutableListOf<String>()
        frenchEmailFormats.add("$cleanFirstName.$cleanLastName@$domain")  // prenom.nom@domaine.com - très courant en France
        frenchEmailFormats.add("$cleanFirstName@$domain")                 // prenom@domaine.com - courant pour les petites entreprises
        frenchEmailFormats.add("$firstInitial$cleanLastName@$domain")     // pnom@domaine.com - assez courant
        frenchEmailFormats.add("$cleanLastName.$cleanFirstName@$domain")  // nom.prenom@domaine.com - utilisé dans certaines entreprises
        frenchEmailFormats.add("$cleanFirstName-$cleanLastName@$domain")  // prenom-nom@domaine.com
        frenchEmailFormats.add("$cleanFirstName$cleanLastName@$domain")   // prenomnom@domaine.com
        frenchEmailFormats.add("$cleanLastName$cleanFirstName@$domain")   // nomprenom@domaine.com
        frenchEmailFormats.add("$firstInitial.$cleanLastName@$domain")    // p.nom@domaine.com
        frenchEmailFormats.add("$cleanFirstName.$cleanLastName@${domain.replace(".com", ".fr")}")  // version .fr
        val bigFrenchCompanies = mapOf(
            "orange" to listOf("$cleanFirstName.$cleanLastName@orange.com", "$cleanFirstName.$cleanLastName@orange.fr"),
            "bnp" to listOf("$cleanFirstName.$cleanLastName@bnpparibas.com", "$firstInitial$cleanLastName@bnpparibas.com"),
            "societe generale" to listOf("$cleanFirstName.$cleanLastName@socgen.com", "$firstInitial$cleanLastName@socgen.com"),
            "total" to listOf("$cleanFirstName.$cleanLastName@totalenergies.com", "$cleanFirstName.$cleanLastName@total.com"),
            "carrefour" to listOf("$cleanFirstName.$cleanLastName@carrefour.com", "$firstInitial$cleanLastName@carrefour.com"),
            "axa" to listOf("$cleanFirstName.$cleanLastName@axa.com", "$cleanFirstName.$cleanLastName@axa.fr"),
            "edf" to listOf("$cleanFirstName.$cleanLastName@edf.fr", "$firstInitial$cleanLastName@edf.fr"),
            "engie" to listOf("$cleanFirstName.$cleanLastName@engie.com", "$firstInitial$cleanLastName@engie.com"),
            "sncf" to listOf("$cleanFirstName.$cleanLastName@sncf.fr", "$firstInitial.$cleanLastName@sncf.fr"),
            "renault" to listOf("$cleanFirstName.$cleanLastName@renault.com", "$firstInitial$cleanLastName@renault.com"),
            "psa" to listOf("$cleanFirstName.$cleanLastName@stellantis.com", "$cleanFirstName.$cleanLastName@mpsa.com"),
            "lvmh" to listOf("$cleanFirstName.$cleanLastName@lvmh.com", "$firstInitial$cleanLastName@lvmh.com"),
            "loreal" to listOf("$cleanFirstName.$cleanLastName@loreal.com", "$firstInitial$cleanLastName@loreal.com"),
            "danone" to listOf("$cleanFirstName.$cleanLastName@danone.com", "$firstInitial$cleanLastName@danone.com"),
            "michelin" to listOf("$cleanFirstName.$cleanLastName@michelin.com", "$firstInitial$cleanLastName@michelin.com"),
            "capgemini" to listOf("$cleanFirstName.$cleanLastName@capgemini.com", "$firstInitial.$cleanLastName@capgemini.com"),
            "atos" to listOf("$cleanFirstName.$cleanLastName@atos.net", "$firstInitial$cleanLastName@atos.net"),
            "sopra" to listOf("$cleanFirstName.$cleanLastName@soprasteria.com", "$firstInitial$cleanLastName@soprasteria.com"),
            "thales" to listOf("$cleanFirstName.$cleanLastName@thalesgroup.com", "$firstInitial$cleanLastName@thalesgroup.com"),
            "airbus" to listOf("$cleanFirstName.$cleanLastName@airbus.com", "$firstInitial$cleanLastName@airbus.com")
        )
        bigFrenchCompanies.forEach {(companyName, emailFormats) -> if (company.lowercase().contains(companyName)) {frenchEmailFormats.addAll(emailFormats)}}
        if (company.lowercase().contains("freelance") || company.lowercase().contains("indépendant") ||
            company.lowercase().contains("consultant") || company.lowercase().contains("auto-entrepreneur")) {
            frenchEmailFormats.add("$cleanFirstName.$cleanLastName@gmail.com")
            frenchEmailFormats.add("$cleanFirstName$cleanLastName@gmail.com")
            frenchEmailFormats.add("$cleanFirstName.$cleanLastName@outlook.fr")
            frenchEmailFormats.add("$cleanFirstName.$cleanLastName@outlook.com")
        }
        return frenchEmailFormats.distinct()
    }

    private fun emptyProspectData(): ProspectData {return ProspectData("", "Nom inconnu", "Prénom inconnu", "", "Nom de famille inconnu", "", emptyList(), "Entreprise inconnue", "Poste inconnu")}
}