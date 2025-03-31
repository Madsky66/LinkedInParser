package manager

import data.ProspectData
import java.io.File

class FileManager {

    fun importFromFile(filePath: String, onImportedProspectData: (ProspectData, Int) -> Unit) {
        val fileToImport = File(filePath)
        if (!fileToImport.exists()) {throw IllegalArgumentException("Le fichier spécifié n'existe pas : $filePath")}
        File(filePath).readLines().drop(1).map {line ->
            val columns = line.split(",")
            onImportedProspectData(
                ProspectData(
                    linkedinURL = "${columns[0].takeIf {it.isNotBlank()}}",
                    firstName = "${columns[1].takeIf {it.isNotBlank()}}",
                    middleName = "${columns[2].takeIf {it.isNotBlank()}}",
                    lastName = "${columns[3].takeIf {it.isNotBlank()}}",
                    email = "${columns[4].takeIf {it.isNotBlank()}}",
                    generatedEmails = columns[5].split(";").map {it.trim()}.filter {it.isNotEmpty()},
                    company = "${columns[6].takeIf {it.isNotBlank()}}",
                    jobTitle = "${columns[7].takeIf {it.isNotBlank()}}"
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