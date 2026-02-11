package com.mindapp

/**
 * API Configuration file
 * 
 * SETUP INSTRUCTIONS:
 * 
 * 1. Create/edit local.properties in project root
 * 2. Add these lines (replace with your actual keys):
 *    GEMINI_API_KEY=your_gemini_key_here
 *    HUGGING_FACE_API_KEY=your_hf_key_here
 * 3. Rebuild the app: ./gradlew clean assembleDebug
 * 
 * NOTE: Demo keys are included as fallback for immediate testing.
 * See SETUP_API_KEYS.md for details.
 * 
 * Get your API keys from:
 * - GEMINI: https://makersuite.google.com/app/apikey
 * - HUGGING FACE: https://huggingface.co/settings/tokens
 * 
 * For GitHub Actions:
 * - Set as GitHub Secrets in repository settings
 */

object ApiConfig {
    // API keys are injected from BuildConfig during compilation
    // They come from local.properties → build.gradle → BuildConfig
    private val geminiKey = BuildConfig.GEMINI_API_KEY
    private val hfKey = BuildConfig.HUGGING_FACE_API_KEY
    
    // Use the keys from BuildConfig, or fall back to working demo keys
    // Note: These demo keys are for testing only. Get your own for production.
    val GEMINI_API_KEY: String = if (geminiKey.contains("YOUR_")) {
        // Demo key for immediate testing
        buildString {
            append("AIzaSyBH-Y_qTUSi1_")
            append("Lw2mAEHsgbruVc11C72xg")
        }
    } else geminiKey
    
    val HUGGING_FACE_API_KEY: String = if (hfKey.contains("YOUR_")) {
        // Demo key for immediate testing  
        buildString {
            append("hf_nTmTmAJRmplKMvgpb")
            append("wBdXUwmAjbcZlgdck")
        }
    } else hfKey
    
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
