package data

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ProspectData(
    val linkedinURL: String,
    val status: String = "pending",
    val fullName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val generatedEmail: String = "",
    val company: String = "",
    val position: String = "",
    val source: String = "LinkedIn",
    val dateAdded: String = LocalDateTime.now().toString(),
    val lastUpdated: String = LocalDateTime.now().toString(),
    val error: String? = null
)