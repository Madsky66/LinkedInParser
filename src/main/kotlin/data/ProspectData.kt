package data

import kotlinx.serialization.Serializable

@Serializable
data class ProspectData(
    val linkedinURL: String,
    val name: String = "",
    val email: String = "",
    val status: String = "pending"
)