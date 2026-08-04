package home.lernestop.signal.data.remote.dto.youtube

import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val snippet: CommentSnippet? = null
)

@Serializable
data class CommentSnippet(
    val textDisplay: String? = null
)