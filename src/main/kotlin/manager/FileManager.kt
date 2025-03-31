package manager

import data.ProspectData
import java.io.File

class FileManager {

    fun importFromFile(filePath: String, onImportedProspectData: (ProspectData, Int) -> Unit) {
        val fileToImport = File(filePath)
        if (!fileToImport.exists()) {throw IllegalArgumentException("Le fichier spécifié n'existe pas : $filePath")}
        File(filePath).readLines().drop(1).map {line ->
            val columns = line.split(",")

            val linkedinURL = columns.getOrNull(0)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val firstName = columns.getOrNull(1)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val middleName = columns.getOrNull(2)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val lastName = columns.getOrNull(3)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val email = columns.getOrNull(4)?.trim()?.takeIf {it.isNotBlank() && it != "null" }?: ""
            val generatedEmails = columns.getOrNull(5)?.split(";")?.map {it.trim()}?.filter {it.isNotEmpty() && it != "null"} ?: emptyList()
            val company = columns.getOrNull(6)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""
            val jobTitle = columns.getOrNull(7)?.trim()?.takeIf {it.isNotBlank() && it != "null"} ?: ""

            onImportedProspectData(
                ProspectData(
                    linkedinURL = linkedinURL,
                    firstName = firstName,
                    middleName = middleName,
                    lastName = lastName,
                    email = email,
                    generatedEmails = generatedEmails,
                    company = company,
                    jobTitle = jobTitle
                ),
                columns.size
            )
        }
    }

    fun exportToFile(prospectData: ProspectData, exportFilePath: String) {
        val fileToExport = File(exportFilePath)
        val extension = exportFilePath.substringAfterLast('.', "").lowercase()

        when (extension) {
            "csv" -> {
                fileToExport.printWriter().use {out ->
                    out.println("LinkedIn URL, First Name, Middle Name, Last Name, Email, Generated Emails, Company, Job Title")
                    out.println("${prospectData.linkedinURL}, ${prospectData.firstName}, ${prospectData.middleName}, ${prospectData.lastName}, ${prospectData.email}, ${prospectData.generatedEmails.joinToString(";")}, ${prospectData.company}, ${prospectData.jobTitle}")
                }
            }
            "xlsx" -> {
                val tempCsvFile = File("${exportFilePath}.temp.csv")
                tempCsvFile.printWriter().use {out ->
                    out.println("LinkedIn URL, First Name, Middle Name, Last Name, Email, Generated Emails, Company, Job Title")
                    out.println("${prospectData.linkedinURL}, ${prospectData.firstName}, ${prospectData.middleName}, ${prospectData.lastName}, ${prospectData.email}, ${prospectData.generatedEmails.joinToString(";")}, ${prospectData.company}, ${prospectData.jobTitle}")
                }
                tempCsvFile.copyTo(fileToExport, overwrite = true)
                tempCsvFile.delete()
            }
            else -> throw IllegalArgumentException("Format de fichier non supporté: $extension")
        }
    }
}