package com.mindapp

/**
 * API Configuration file
 * 
 * IMPORTANT: Replace the placeholder values below with your actual API keys:
 * 
 * 1. GEMINI_API_KEY: Get your API key from Google AI Studio
 *    - Visit: https://makersuite.google.com/app/apikey
 *    - Sign in with your Google account
 *    - Click "Create API Key"
 *    - Copy the key and paste it below
 * 
 * 2. HUGGING_FACE_API_KEY: Get your API key from Hugging Face
 *    - Visit: https://huggingface.co/settings/tokens
 *    - Sign in or create an account
 *    - Click "New token"
 *    - Give it a name (e.g., "MindApp")
 *    - Select "Read" permission
 *    - Copy the token and paste it below
 * 
 * NOTE: Never commit this file with real API keys to public repositories!
 * Consider using environment variables or a secrets management system for production.
 */

object ApiConfig {
    // TODO: Replace with your actual Gemini API key (get from https://makersuite.google.com/app/apikey)
    const val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY_HERE"
    
    // TODO: Replace with your actual Hugging Face API key (get from https://huggingface.co/settings/tokens)
    const val HUGGING_FACE_API_KEY = "YOUR_HUGGING_FACE_API_KEY_HERE"
    
    // Gemini API endpoint
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    
    // Hugging Face API endpoint for emotion detection
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
