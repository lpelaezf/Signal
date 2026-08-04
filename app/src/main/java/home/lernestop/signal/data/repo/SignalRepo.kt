package home.lernestop.signal.data.repo

import home.lernestop.signal.core.exception.SignalException
import home.lernestop.signal.data.local.dao.VideoDao
import home.lernestop.signal.data.local.entity.VideoEntity
import home.lernestop.signal.data.local.projection.VideoSummaryWithSignal
import home.lernestop.signal.data.local.projection.VideoSummaryWithStatistics
import home.lernestop.signal.data.mapper.toStringResponse
import home.lernestop.signal.data.mapper.toVideoComment
import home.lernestop.signal.data.mapper.toVideoEntity
import home.lernestop.signal.data.model.VideoComment
import home.lernestop.signal.data.remote.api.GeminiApiService
import home.lernestop.signal.data.remote.api.YouTubeApiService
import home.lernestop.signal.data.remote.dto.error.ErrorResponse
import home.lernestop.signal.data.remote.dto.gemini.GeminiRequest
import home.lernestop.signal.data.remote.dto.gemini.Input
import home.lernestop.signal.data.remote.dto.youtube.VideoDto
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import okio.IOException
import retrofit2.HttpException
import java.util.Locale
import javax.inject.Inject


class SignalRepo @Inject constructor(
    private val videoDao: VideoDao,
    private val youTubeApiService: YouTubeApiService,
    private val geminiApiService: GeminiApiService,
    private val json: Json,
) {
    fun getVideos(): Flow<List<VideoSummaryWithStatistics>> = videoDao.getAllVideos()

    suspend fun getSignal(videoId: String): VideoSummaryWithSignal = videoDao.getVideoSignal(videoId)

    suspend fun deleteVideos(videoIds: List<String>) = videoDao.deleteVideoById(videoIds)

    /**
     * Orchestrates the process of fetching video metadata, its comments, generating a summary
     * using AI, and persisting the result in the local database.
     * 
     * @param videoId The YouTube video ID.
     * @throws SignalException if any step in the process fails.
     */
    suspend fun findVideo(videoId: String) {

        val video = fetchVideo(videoId)
        var signal: String? = null

        val commentCount = video.statistics?.commentCount?.toLongOrNull() ?: 0L

        try {
            if (commentCount > 0) {
                val comments = fetchComments(videoId)
                signal = if (comments.isNotEmpty()) fetchSignal(comments)
                else {
                    throw SignalException.ResourceNotFoundException(
                        "Could not retrieve the comments"
                    )
                }
            } else throw SignalException.CommentException()

        } catch (e: SignalException.ResourceNotFoundException) {
            throw SignalException.CommentException(e.message.orEmpty())
        } catch (e: SignalException.CommentException) {
            throw e
        } catch (e: SignalException.GenerateInteractionException) {
            throw e
        } finally {
            insertVideo(video.toVideoEntity(signal))
        }

    }

    /**
     * Fetches video metadata from the YouTube API.
     * 
     * @param videoId The YouTube video ID.
     * @return A [VideoDto] containing video details.
     * @throws SignalException.VideoNotFoundException if the video doesn't exist.
     * @throws SignalException.QuotaExceededException if API quota is reached.
     */
    private suspend fun fetchVideo(videoId: String): VideoDto {

        try {
            val response = youTubeApiService.getVideo(videoId)

            val items = response.items ?: throw SignalException.ItemsNotFoundException(
                "Could not retrieve the items[] property from videos"
            )

            return items.firstOrNull() ?: throw SignalException.ResourceNotFoundException(
                "There was a problem with video resource, items[]"
            )
        } catch (e: SignalException) {
            throw SignalException.VideoNotFoundException(e.message.orEmpty())

        } catch (e: HttpException) {
            when(e.code()) {
                404 -> throw SignalException.VideoNotFoundException()
                403 -> {
                    val responseBody = e.response()?.errorBody()?.string() ?: ""

                    if (!responseBody.isEmpty()) {
                        val errorResponse = json.decodeFromString<ErrorResponse>(responseBody)
                        val code = errorResponse.error?.code ?: ""

                        if (code.contains("quotaExceeded")) throw SignalException.QuotaExceededException()
                        else throw SignalException.RemoteServiceException(e.message.orEmpty())

                    } else {
                        throw SignalException.RemoteServiceException(e.message.orEmpty())
                    }
                }
                else -> throw SignalException.RemoteServiceException(e.message.orEmpty())
            }
        } catch (e: IOException) {
            throw SignalException.NetworkException(e.message.orEmpty())
        }

    }

    /**
     * Retrieves top-level comments for a specific video.
     * 
     * @param videoId The YouTube video ID.
     * @return A list of [VideoComment]s.
     * @throws SignalException.CommentException if comments cannot be retrieved.
     */
    private suspend fun fetchComments(videoId: String): List<VideoComment> {
        try {
            val response = youTubeApiService.getComments(videoId)

            val items = response.items ?: throw SignalException.ItemsNotFoundException(
                "Could not retrieve the items[] property from commentThreads"
            )

            return items.map { it.toVideoComment() }
        } catch (e: SignalException) {
            throw SignalException.CommentException(e.message.orEmpty())

        } catch (e: HttpException) {
            throw SignalException.CommentException(e.message.orEmpty())

        } catch (e: IOException) {
            throw SignalException.CommentException(e.message.orEmpty())
        }
    }

    /**
     * Generates a concise summary of the provided comments using Gemini AI.
     * 
     * @param comments The list of comments to analyze.
     * @return A string containing the generated signal/summary.
     * @throws SignalException.GenerateInteractionException if AI generation fails.
     */
    private suspend fun fetchSignal(comments: List<VideoComment>): String {
        val promptDefault = """
            Analyze the video's comments and provide a clear, concise summary of the
            overall opinion. Highlight only the most frequently mentioned ideas, the
            predominant sentiment, and any significant disagreements.
            Avoid repeating information or referring to individual comments.
            Limit the response to a maximum of 150 words.
        """.trimIndent()
        
        val userLanguage = "Answer in ${Locale.getDefault().displayLanguage} "
        val promptFinal = userLanguage + promptDefault + Json.encodeToString(comments)

        val request = GeminiRequest(
            model = "gemini-3.1-flash-lite-preview",
            input = listOf(
                Input(
                type = "text",
                text = promptFinal
                )
            )
        )

        try {
            val response = geminiApiService.generateContent(request)

            return response.toStringResponse().ifEmpty { throw SignalException.GenerateInteractionException() }
        } catch (e: HttpException) {
            when(e.code()) {
                429 -> {
                    val responseBody = e.response()?.errorBody()?.string() ?: ""

                    if (!responseBody.isEmpty()) {
                        val errorResponse = json.decodeFromString<ErrorResponse>(responseBody)
                        val code = errorResponse.error?.code ?: ""

                        if (code.contains("quotaExceeded")) throw SignalException.QuotaExceededException()
                        else throw SignalException.GenerateInteractionException(e.message.orEmpty())

                    } else {
                        throw SignalException.GenerateInteractionException(e.message.orEmpty())
                    }
                }
                else -> throw SignalException.GenerateInteractionException(e.message.orEmpty())
            }
        } catch (e: IOException) {
            throw SignalException.GenerateInteractionException(e.message.orEmpty())
        }
    }

    private suspend fun insertVideo(videoEntity: VideoEntity) {
        videoDao.insertVideo(videoEntity)
    }
}