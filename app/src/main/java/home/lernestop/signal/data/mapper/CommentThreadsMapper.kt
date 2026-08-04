package home.lernestop.signal.data.mapper

import home.lernestop.signal.data.model.VideoComment
import home.lernestop.signal.data.remote.dto.youtube.CommentThreadsDto

fun CommentThreadsDto.toVideoComment(): VideoComment {
    return VideoComment(
        text = this.snippet?.topLevelComment?.snippet?.textDisplay ?: "",
        replies = this.replies?.comments?.map { it.snippet?.textDisplay ?: "" } ?: listOf(),
    )
}