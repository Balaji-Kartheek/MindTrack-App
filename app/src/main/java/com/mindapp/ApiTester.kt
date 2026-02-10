package com.mindapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Utility to test API keys and connections.
 * Use this to verify if your API keys are working.
 */
object ApiTester {

    private val client = OkHttpClient()

    /**
     * Test Gemini API Key
     * Returns: Pair<Boolean, String> - (isSuccess, message)
     */
    suspend fun testGeminiApi(apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Say hello in one word")
                            })
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                if (jsonResponse.has("candidates")) {
                    Pair(true, "✅ Gemini API is working!\n\nResponse: ${jsonResponse.toString(2)}")
                } else {
                    Pair(false, "❌ Gemini API returned unexpected format:\n$responseBody")
                }
            } else {
                Pair(false, "❌ Gemini API failed!\n\nStatus: ${response.code}\nMessage: ${response.message}\n\nBody: $responseBody")
            }
        } catch (e: IOException) {
            Pair(false, "❌ Network Error: ${e.message}\n\nCheck your internet connection.")
        } catch (e: Exception) {
            Pair(false, "❌ Error: ${e.message}\n\n${e.stackTraceToString()}")
        }
    }

    /**
     * Test Hugging Face API Key
     * Returns: Pair<Boolean, String> - (isSuccess, message)
     */
    suspend fun testHuggingFaceApi(apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api-inference.huggingface.co/models/j-hartmann/emotion-english-distilroberta-base"
            
            val jsonBody = JSONObject().apply {
                put("inputs", "I am feeling happy today")
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONArray(responseBody)
                if (jsonResponse.length() > 0) {
                    Pair(true, "✅ Hugging Face API is working!\n\nResponse: ${jsonResponse.toString(2)}")
                } else {
                    Pair(false, "❌ Hugging Face API returned empty response")
                }
            } else {
                Pair(false, "❌ Hugging Face API failed!\n\nStatus: ${response.code}\nMessage: ${response.message}\n\nBody: $responseBody")
            }
        } catch (e: IOException) {
            Pair(false, "❌ Network Error: ${e.message}\n\nCheck your internet connection.")
        } catch (e: Exception) {
            Pair(false, "❌ Error: ${e.message}\n\n${e.stackTraceToString()}")
        }
    }

    /**
     * Test current BuildConfig keys
     */
    suspend fun testCurrentConfiguration(): Pair<String, String> {
        val geminiResult = testGeminiApi(ApiConfig.GEMINI_API_KEY)
        val huggingFaceResult = testHuggingFaceApi(ApiConfig.HUGGING_FACE_API_KEY)
        
        return Pair(
            "Gemini: ${if (geminiResult.first) "✅ Working" else "❌ Failed"}\n${geminiResult.second}",
            "Hugging Face: ${if (huggingFaceResult.first) "✅ Working" else "❌ Failed"}\n${huggingFaceResult.second}"
        )
    }
}
