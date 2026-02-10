package com.mindapp

/**
 * API Configuration file
 * 
 * IMPORTANT: API keys are now injected during build from environment variables or GitHub Secrets.
 * 
 * For GitHub Actions:
 * - Set GEMINI_API_KEY and HUGGING_FACE_API_KEY as GitHub Secrets in your repository settings
 * 
 * For local builds:
 * - Export environment variables before building:
 *   export GEMINI_API_KEY="your_key_here"
 *   export HUGGING_FACE_API_KEY="your_key_here"
 * 
 * Get your API keys from:
 * 1. GEMINI_API_KEY: https://makersuite.google.com/app/apikey
 * 2. HUGGING_FACE_API_KEY: https://huggingface.co/settings/tokens
 */

object ApiConfig {
    // API keys are injected from BuildConfig during compilation
    // Keys are read from local.properties at build time
    // Make sure to add your keys to local.properties before building
    val GEMINI_API_KEY: String = BuildConfig.GEMINI_API_KEY
    
    val HUGGING_FACE_API_KEY: String = BuildConfig.HUGGING_FACE_API_KEY
    
    // Gemini API endpoint
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    
    // Hugging Face API endpoint for emotion detection
    // Note: If you get 410 errors, the HF API key might be invalid or expired
    const val HUGGING_FACE_BASE_URL = "https://api-inference.huggingface.co/models/"
    const val EMOTION_MODEL = "j-hartmann/emotion-english-distilroberta-base"
    
    /**
     * Returns true if Gemini API key is set and looks valid (not placeholder).
     */
    fun isGeminiConfigured(): Boolean {
        return GEMINI_API_KEY.isNotBlank() &&
                !GEMINI_API_KEY.contains("YOUR_") &&
                GEMINI_API_KEY.length > 10
    }
    
    /**
     * Returns true if Hugging Face API key is set and looks valid (not placeholder).
     */
    fun isHuggingFaceConfigured(): Boolean {
        return HUGGING_FACE_API_KEY.isNotBlank() &&
                !HUGGING_FACE_API_KEY.contains("YOUR_") &&
                HUGGING_FACE_API_KEY.length > 10
    }
}
