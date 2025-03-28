package data

import kotlinx.serialization.Serializable

@Serializable
data class ProspectData(
    val linkedinURL: String = "",
    val fullName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val email: String = "",
    val generatedEmails: List<String> = emptyList(),
    val company: String = "",
    val jobTitle: String = "",
    val error: String? = null
)