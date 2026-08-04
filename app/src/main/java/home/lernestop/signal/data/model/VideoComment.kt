package home.lernestop.signal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoComment(
    val text: String,
    val replies: List<String>? = null
)
