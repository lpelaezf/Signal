package home.lernestop.signal.data.local.projection

data class VideoSummaryWithSignal(
    val title: String,
    val creator: String,
    val signal: String? = null,
)
