package com.mindapp.chatbot

import com.mindapp.ApiConfig

/**
 * Ordered list: try primary first; on 503/429/500 retry with lighter models.
 */
object GeminiModelFallback {
    private val modelIds = listOf(
        "gemini-2.5-flash",
        "gemini-2.0-flash",
        "gemini-1.5-flash"
    )

    fun modelIdsOrdered(): List<String> = modelIds

    fun endpointForModel(modelId: String): String {
        val base = ApiConfig.GEMINI_BASE_URL.trimEnd('/')
        return "$base/models/$modelId:generateContent"
    }
}
