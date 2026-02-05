package com.mindapp.mood

import com.mindapp.ApiConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for Hugging Face Emotion Detection API
 * 
 * Uses Hugging Face's emotion detection model to analyze
 * user's emotional state from text input
 */
interface EmotionService {
    
    /**
     * Analyzes emotion from text input
     * 
     * @param authorization Hugging Face API token
     * @param request The text input for emotion detection
     * @return Response containing emotion analysis results
     */
    @POST(ApiConfig.EMOTION_MODEL)
    suspend fun detectEmotion(
        @Header("Authorization") authorization: String = "Bearer ${ApiConfig.HUGGING_FACE_API_KEY}",
        @Body request: EmotionRequest
    ): Response<List<EmotionResult>>
}

/**
 * Request model for emotion detection
 */
data class EmotionRequest(
    val inputs: String
)

/**
 * Response model for emotion detection
 */
data class EmotionResult(
    val label: String,
    val score: Double
)
