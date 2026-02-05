package com.mindapp.chatbot

import com.mindapp.ApiConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for Gemini API
 * 
 * Uses Google's Generative Language API (Gemini) to provide
 * AI-powered chatbot responses about digital wellbeing
 */
interface GeminiService {
    
    /**
     * Sends a chat message to Gemini API
     * 
     * @param apiKey The Gemini API key (passed as query parameter)
     * @param request The chat request body
     * @return Response containing the AI-generated message
     */
    @POST("models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String = ApiConfig.GEMINI_API_KEY,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

/**
 * Request model for Gemini API
 */
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

/**
 * Response model for Gemini API
 */
data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content
)
