package home.lernestop.signal.data.remote.api

import home.lernestop.signal.BuildConfig
import home.lernestop.signal.data.remote.dto.gemini.GeminiRequest
import home.lernestop.signal.data.remote.dto.gemini.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GeminiApiService {

    @POST("interactions")
    suspend fun generateContent(
        @Body request: GeminiRequest,
        @Header("x-goog-api-key") apiKey: String = BuildConfig.GEMINI_API_KEY,
        @Header("Content-Type") contentType: String = "application/json"
    ): GeminiResponse

}
