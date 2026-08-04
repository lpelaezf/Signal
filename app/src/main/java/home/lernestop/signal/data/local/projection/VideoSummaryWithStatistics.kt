package home.lernestop.signal.data.local.projection

data class VideoSummaryWithStatistics(
    val videoId: String,
    val thumbnail: String,
    val title: String,
    val creator: String,
    val likesCount: Long,
    val commentsCount: Long,
    val viewsCount: Long,
)
