package home.lernestop.signal.ui.mapper

import home.lernestop.signal.core.extension.toFormattedCount
import home.lernestop.signal.data.local.projection.VideoSummaryWithStatistics
import home.lernestop.signal.ui.model.VideoCard

fun VideoSummaryWithStatistics.toVideoUi(): VideoCard {
    return VideoCard(
        id = this.videoId,
        thumbnail = this.thumbnail,
        title = this.title,
        creator = this.creator,
        viewsCount = this.viewsCount.toFormattedCount(),
        likesCount = this.likesCount.toFormattedCount(),
        commentsCount = this.commentsCount.toFormattedCount(),
    )
}