package home.lernestop.signal.data.mapper

import home.lernestop.signal.data.local.entity.VideoEntity
import home.lernestop.signal.data.local.projection.VideoStatisticsUpdate
import home.lernestop.signal.data.remote.dto.youtube.VideoDto
import kotlin.time.Clock

fun VideoDto.toVideoEntity(signal: String? = null): VideoEntity {
    return VideoEntity(
        videoId = this.id ?: "",
        insertedAt = Clock.System.now(),
        title = this.snippet?.title ?: "",
        creator = this.snippet?.channelTitle ?: "",
        thumbnail = this.snippet?.thumbnails?.run { high?.url ?: medium?.url ?: default?.url },
        likesCount = this.statistics?.likeCount?.toLongOrNull() ?: 0L,
        commentsCount = this.statistics?.commentCount?.toLongOrNull() ?: 0L,
        viewsCount = this.statistics?.viewCount?.toLongOrNull() ?: 0L,
        signal = signal,
    )
}

fun VideoDto.toVideoStatisticsUpdate(): VideoStatisticsUpdate {
    return VideoStatisticsUpdate(
        videoId = this.id ?: "",
        likesCount = this.statistics?.likeCount?.toLongOrNull() ?: 0L,
        commentsCount = this.statistics?.commentCount?.toLongOrNull() ?: 0L,
        viewsCount = this.statistics?.viewCount?.toLongOrNull() ?: 0L,
    )
}