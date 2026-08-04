package home.lernestop.signal.data.remote.dto.error

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: ErrorBody? = null
)
@Serializable
data class ErrorBody(
    val code: String? = null
)