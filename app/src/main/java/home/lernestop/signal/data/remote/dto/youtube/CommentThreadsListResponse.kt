package home.lernestop.signal.data.remote.dto.youtube

import kotlinx.serialization.Serializable

@Serializable
data class CommentThreadsListResponse(
    val items: List<CommentThreadsDto>? = null
)

@Serializable
data class CommentThreadsDto(
    val snippet: CommentThreadsSnippet? = null,
    val replies: Replay? = null
)

@Serializable
data class CommentThreadsSnippet(
    val topLevelComment: CommentDto? = null
)

@Serializable
data class Replay(
    val comments: List<CommentDto>? = null
)