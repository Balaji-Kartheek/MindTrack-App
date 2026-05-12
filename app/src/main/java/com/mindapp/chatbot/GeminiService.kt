package com.mindapp.chatbot

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Retrofit interface for Gemini API (full URL per call for model fallback).
 */
interface GeminiService {

    @POST
    suspend fun generateContent(
        @Url url: String,
        @Query("key") apiKey: String,
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
