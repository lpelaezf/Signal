package home.lernestop.signal.data.local.projection

data class VideoStatisticsUpdate(
    val videoId: String? = null,
    val likesCount: Long? = null,
    val commentsCount: Long? = null,
    val viewsCount: Long? = null,
)
