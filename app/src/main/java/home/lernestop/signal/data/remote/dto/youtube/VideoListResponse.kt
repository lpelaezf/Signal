package home.lernestop.signal.data.remote.dto.youtube

import kotlinx.serialization.Serializable

@Serializable
data class VideoListResponse(
    val items: List<VideoDto>? = null,
)

@Serializable
data class VideoDto(
    val id: String? = null,
    val snippet: VideoSnippet? = null,
    val statistics: Statistics? = null,
)

@Serializable
data class VideoSnippet(
    val title: String? = null,
    val thumbnails: Thumbnail? = null,
    val channelTitle: String? = null,
)

@Serializable
data class Thumbnail(
    val default: ThumbnailDefaultUrl? = null,
)

@Serializable
data class ThumbnailDefaultUrl(
    val url: String? = null,
)

@Serializable
data class Statistics(
    val viewCount: String? = null,
    val likeCount: String? = null,
    val commentCount: String? = null,
)