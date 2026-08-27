package home.lernestop.signal.data.remote.api

import home.lernestop.signal.BuildConfig
import home.lernestop.signal.data.remote.dto.youtube.CommentThreadsListResponse
import home.lernestop.signal.data.remote.dto.youtube.VideoListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("videos")
    suspend fun getVideos(
        @Query("id") videoId: List<String>,
        @Query("key") apiKey: String = BuildConfig.YOUTUBE_API_KEY,
        @Query("part") part: String = "id,snippet,statistics",
        @Query("fields") fields: String = """
            items(
                id,
                snippet(
                    title,
                    thumbnails,
                    channelTitle
                ),
                statistics(
                    viewCount,
                    likeCount,
                    commentCount
                )
            )
        """.filterNot(Char::isWhitespace)
    ): VideoListResponse

    @GET("videos")
    suspend fun getVideoStatistics(
        @Query("id") videoId: String,
        @Query("key") apiKey: String = BuildConfig.YOUTUBE_API_KEY,
        @Query("part") part: String = "id,statistics",
        @Query("fields") fields: String = """
            items(
                id,
                statistics(
                    viewCount,
                    likeCount,
                    commentCount
                )
            )
        """.filterNot(Char::isWhitespace)
    ): VideoListResponse

    @GET("commentThreads")
    suspend fun getComments(
        @Query("videoId") videoId: String,
        @Query("key") apiKey: String = BuildConfig.YOUTUBE_API_KEY,
        @Query("maxResults") maxResult: Int = 100,
        @Query("order") order: String = "relevance",
        @Query("part") part: String = "snippet,replies",
        @Query("fields") fields: String = """
            items(
                snippet/topLevelComment/snippet/textDisplay,
                replies/comments/snippet/textDisplay
            )
        """.filterNot(Char::isWhitespace)
    ): CommentThreadsListResponse
}