package home.lernestop.signal.ui.model

data class VideoCard(
    val id: String,
    val thumbnail: String,
    val title: String,
    val creator: String,
    val viewsCount: String,
    val likesCount: String,
    val commentsCount: String,
)