package home.lernestop.signal.data.remote.dto.gemini

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val model: String,
    val input: List<Input>,
)

@Serializable
data class Input(
    val type: String,
    val text: String,
)
