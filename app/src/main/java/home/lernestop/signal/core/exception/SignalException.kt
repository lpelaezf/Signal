package home.lernestop.signal.core.exception

sealed class SignalException(message: String): Exception(message) {
    data class ItemsNotFoundException(
        val errorMessage: String = "Items not found",
    ): SignalException(errorMessage)
    data class ResourceNotFoundException(
        val errorMessage: String = "Resource not found",
    ): SignalException(errorMessage)

    data class VideoNotFoundException(
        val errorMessage: String = "Video not found",
    ): SignalException(errorMessage)

    data class CommentException(
        val errorMessage: String = "No comments or could not retrieve them",
    ): SignalException(errorMessage)

    data class GenerateInteractionException(
        val errorMessage: String = "Interaction couldn't be generated",
    ): SignalException(errorMessage)

    data class RemoteServiceException(
        val errorMessage: String = "There was an error with the request",
    ): SignalException(errorMessage)

    data class NetworkException(
        val errorMessage: String = "There was a network error",
    ): SignalException(errorMessage)

    data class QuotaExceededException(
        val errorMessage: String = "The request cannot be completed because the quota has been exceeded.",
    ): SignalException(errorMessage)
}