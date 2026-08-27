package home.lernestop.signal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import home.lernestop.signal.data.local.entity.VideoEntity
import home.lernestop.signal.data.local.projection.VideoStatisticsUpdate
import home.lernestop.signal.data.local.projection.VideoSummaryWithSignal
import home.lernestop.signal.data.local.projection.VideoSummaryWithStatistics
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Update(entity = VideoEntity::class)
    suspend fun updateVideoStatistics(videoStatistics: List<VideoStatisticsUpdate>)

    @Query("DELETE FROM video WHERE videoId IN (:videoIds)")
    suspend fun deleteVideoById(videoIds: List<String>)

    @Query(
        "SELECT videoId, title, creator, thumbnail, likesCount, commentsCount, viewsCount " +
                "FROM video " +
                "ORDER BY insertedAt DESC"
    )
    fun getAllVideos(): Flow<List<VideoSummaryWithStatistics>>

    @Query("SELECT title, creator, signal FROM video WHERE videoId = :videoId")
    suspend fun getVideoSignal(videoId: String): VideoSummaryWithSignal

    @Query("SELECT videoId FROM video")
    suspend fun getAllVideoIds(): List<String>
}
