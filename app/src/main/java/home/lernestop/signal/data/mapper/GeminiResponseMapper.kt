package home.lernestop.signal.data.mapper

import home.lernestop.signal.data.remote.dto.gemini.GeminiResponse

fun GeminiResponse.toStringResponse(): String {
    return this.steps?.firstOrNull {it.type == "model_output"}?.content?.firstOrNull()?.text ?: ""
}