package home.lernestop.signal.data.remote.dto.gemini

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val steps: List<Step>? = null
)

@Serializable
data class Step(
    val type: String? = null,
    val content: List<Content>? = null
)

@Serializable
data class Content(
    val text: String? = null
)