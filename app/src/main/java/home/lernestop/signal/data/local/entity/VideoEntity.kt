package home.lernestop.signal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "video")
data class VideoEntity(
    @PrimaryKey val videoId: String,
    val insertedAt: Instant,
    val title: String,
    val creator: String,
    val likesCount: Long,
    val commentsCount: Long,
    val viewsCount: Long,
    val thumbnail: String? = null,
    val signal: String? = null,
)