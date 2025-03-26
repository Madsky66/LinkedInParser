package data

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class ProspectData(
    val linkedinURL: String,
    val status: ProspectStatus = ProspectStatus.PENDING,
    val fullName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val generatedEmail: String = "",
    val company: String = "",
    val position: String = "",
    val source: String = "LinkedIn",
    val dateAdded: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val lastUpdated: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val error: String? = null
) {
    fun isValidLinkedInURL(): Boolean {return linkedinURL.startsWith("https://www.linkedin.com/in/") || linkedinURL.startsWith("https://linkedin.com/in/")}
}

enum class ProspectStatus {PENDING, COMPLETED, IN_PROGRESS, ERROR}