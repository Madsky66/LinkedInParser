package data

import kotlinx.serialization.Serializable

@Serializable
data class ProspectData(
    val linkedinURL: String,
    val status: String = "pending",
    val lastName: String = "",
    val name: String = "",
    val email: String = "",
    val generatedEmail: String = "",
    val company: String = "",
    val position: String = "",
    val source: String = "LinkedIn"
)