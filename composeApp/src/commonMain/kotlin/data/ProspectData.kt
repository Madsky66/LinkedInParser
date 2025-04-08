package data

import kotlinx.serialization.Serializable

@Serializable
data class ProspectData(
    val linkedinUrl: String = "",
    val fullName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val phoneNumber:  String = "",
    val email: String = "",
    val generatedEmails: List<String> = emptyList(),
    val company: String = "",
    val jobTitle: String = "",
)