package com.mindapp.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mindapp.ApiConfig
import com.mindapp.R
import com.mindapp.databinding.FragmentMoodCheckBinding
import com.mindapp.notification.NotificationHelper
import com.mindapp.usage.UsageStatsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Fragment for Emotion Detection and Mood Tracking
 * 
 * Allows users to:
 * - Input how they're feeling (text)
 * - Detect emotions using Hugging Face API
 * - View emotion analysis results
 * - See correlation between emotions and app usage patterns
 */
class MoodCheckFragment : Fragment() {

    private var _binding: FragmentMoodCheckBinding? = null
    private val binding get() = _binding!!
    
    private var emotionService: EmotionService? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupRetrofit()
            binding.btnAnalyze.setOnClickListener { analyzeEmotion() }
            binding.etMoodInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    analyzeEmotion()
                    true
                } else false
            }
        } catch (e: Exception) {
            android.util.Log.e("MoodCheckFragment", "onViewCreated", e)
        }
    }

    /**
     * Sets up Retrofit for Hugging Face API calls. Safe even if API key is not set.
     */
    private fun setupRetrofit() {
        try {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(ApiConfig.HUGGING_FACE_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            emotionService = retrofit.create(EmotionService::class.java)
        } catch (e: Exception) {
            android.util.Log.e("MoodCheckFragment", "setupRetrofit failed", e)
            emotionService = null
        }
    }

    /**
     * Analyzes emotion from user input
     */
    private fun analyzeEmotion() {
        val inputText = binding.etMoodInput.text.toString().trim()
        if (inputText.isEmpty()) {
            binding.tvEmotionResult.text = "💭 Please enter how you're feeling"
            binding.tvEmotionResult.visibility = View.VISIBLE
            return
        }

        if (inputText.length < 3) {
            binding.tvEmotionResult.text = "💬 Please enter at least 3 characters to analyze your mood"
            binding.tvEmotionResult.visibility = View.VISIBLE
            return
        }

        if (!ApiConfig.isHuggingFaceConfigured()) {
            binding.tvEmotionResult.text = 
                "⚠️ Configuration Required\n\n" +
                "Hugging Face API key is not configured.\n\n" +
                "Please add your API key to local.properties and rebuild the app."
            binding.tvEmotionResult.visibility = View.VISIBLE
            return
        }

        val service = emotionService
        if (service == null) {
            binding.tvEmotionResult.text = 
                "❌ Service Error\n\n" +
                "Mood service is not available.\n" +
                "Please restart the app."
            binding.tvEmotionResult.visibility = View.VISIBLE
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnAnalyze.isEnabled = false
        binding.tvEmotionResult.visibility = View.GONE
        binding.tvCorrelation.visibility = View.GONE
        val ctx = context

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = EmotionRequest(inputs = inputText)
                val response = service.detectEmotion(request = request)

                withContext(Dispatchers.Main) {
                    _binding?.progressBar?.visibility = View.GONE
                    _binding?.btnAnalyze?.isEnabled = true
                    if (response.isSuccessful) {
                        val emotions = response.body()
                        if (emotions != null && emotions.isNotEmpty()) {
                            displayEmotionResults(emotions, inputText)
                            correlateWithUsage(emotions)
                        } else {
                            _binding?.tvEmotionResult?.text = "❌ No Response\n\nThe API returned an empty response. Please try again."
                            _binding?.tvEmotionResult?.visibility = View.VISIBLE
                        }
                    } else {
                        val errorMsg = when (response.code()) {
                            401 -> "🔑 Invalid API Key\n\nYour Hugging Face API key is invalid or expired.\n\nGet a new key at:\nhttps://huggingface.co/settings/tokens"
                            503 -> "⏳ Model Loading\n\nThe emotion detection model is loading.\n\nPlease wait 30 seconds and try again."
                            else -> "❌ API Error ${response.code()}\n\n${response.message()}\n\nPlease check your API key and internet connection."
                        }
                        _binding?.tvEmotionResult?.text = errorMsg
                        _binding?.tvEmotionResult?.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _binding?.progressBar?.visibility = View.GONE
                    _binding?.btnAnalyze?.isEnabled = true
                    val errorMsg = when {
                        e.message?.contains("Unable to resolve host") == true -> 
                            "🌐 Network Error\n\nPlease check your internet connection and try again."
                        e.message?.contains("timeout") == true -> 
                            "⏱️ Timeout Error\n\nThe request took too long. Please try again."
                        e.message?.contains("410") == true || e.message?.contains("Gone") == true ->
                            "⚠️ API Update Required\n\nThe Hugging Face API has been updated.\n\nPlease get a new API key at:\nhttps://huggingface.co/settings/tokens"
                        else -> 
                            "❌ Error\n\n${e.message ?: "Unknown error"}\n\nPlease check your internet connection and API key."
                    }
                    _binding?.tvEmotionResult?.text = errorMsg
                    _binding?.tvEmotionResult?.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Displays emotion analysis results
     */
    private fun displayEmotionResults(emotions: List<EmotionResult>, inputText: String) {
        if (emotions.isEmpty()) return
        val sortedEmotions = emotions.sortedByDescending { it.score }
        val topEmotion = sortedEmotions.first()
        val emotionText = buildString {
            append("🎯 Primary Emotion: ${topEmotion.label.replaceFirstChar { it.uppercase() }}\n\n")
            append("📊 Confidence Breakdown:\n\n")
            sortedEmotions.forEach { emotion ->
                val percentage = (emotion.score * 100).toInt()
                val bar = "█".repeat(percentage / 5)
                append("${emotion.label.padEnd(10)} $bar $percentage%\n")
            }
            append("\n💬 What you said:\n\"$inputText\"")
        }
        _binding?.tvEmotionResult?.text = emotionText
        _binding?.tvEmotionResult?.visibility = View.VISIBLE
        val emoji = getEmotionEmoji(topEmotion.label)
        _binding?.tvEmotionEmoji?.text = "$emoji"
        _binding?.tvEmotionEmoji?.visibility = View.VISIBLE
        _binding?.tvEmotionEmoji?.textSize = 48f
    }

    /**
     * Correlates detected emotion with app usage patterns
     */
    private fun correlateWithUsage(emotions: List<EmotionResult>) {
        val ctx = context ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!UsageStatsHelper.hasUsageStatsPermission(ctx)) {
                    withContext(Dispatchers.Main) {
                        _binding?.tvCorrelation?.text = "Usage stats permission not granted. Enable it to see correlations."
                        _binding?.tvCorrelation?.visibility = View.VISIBLE
                    }
                    return@launch
                }
                val topEmotion = emotions.sortedByDescending { it.score }.firstOrNull() ?: return@launch
                val totalScreenTime = UsageStatsHelper.getTotalScreenTime(ctx)
                val socialMediaUsage = UsageStatsHelper.getSocialMediaUsage(ctx)
                val topApps = UsageStatsHelper.getTopApps(ctx, 3)
                val correlationText = buildCorrelationText(topEmotion.label, totalScreenTime, socialMediaUsage, topApps)
                withContext(Dispatchers.Main) {
                    _binding?.tvCorrelation?.text = correlationText
                    _binding?.tvCorrelation?.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _binding?.tvCorrelation?.text = "Unable to correlate with usage stats."
                    _binding?.tvCorrelation?.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Builds correlation text between emotion and usage
     */
    private fun buildCorrelationText(
        emotion: String,
        totalTime: Long,
        socialMediaTime: Long,
        topApps: List<com.mindapp.usage.AppUsageInfo>
    ): String {
        val emotionLower = emotion.lowercase()
        val totalTimeFormatted = UsageStatsHelper.formatTime(totalTime)
        val socialMediaTimeFormatted = UsageStatsHelper.formatTime(socialMediaTime)
        
        val correlation = buildString {
            append("📊 DIGITAL WELLBEING INSIGHTS\n")
            append("═".repeat(30) + "\n\n")
            append("😊 Emotion: ${emotion.replaceFirstChar { it.uppercase() }}\n\n")
            append("📱 Today's Usage:\n")
            append("   ⏱️  Screen Time: $totalTimeFormatted\n")
            append("   📱 Social Media: $socialMediaTimeFormatted\n")
            
            if (topApps.isNotEmpty()) {
                append("   🔝 Top Apps: ${topApps.take(2).joinToString(", ") { it.appName }}\n")
            }
            
            append("\n" + "─".repeat(30) + "\n\n")
            
            // Add personalized insights
            when {
                emotionLower.contains("sad") || emotionLower.contains("anger") || 
                emotionLower.contains("fear") -> {
                    val threeHours = 3 * 60 * 60 * 1000L
                    append("💭 PERSONALIZED ADVICE:\n\n")
                    if (socialMediaTime > threeHours) {
                        append("⚠️ We noticed you've spent $socialMediaTimeFormatted on social media today.\n\n" +
                                "High social media use has been linked to negative emotions. Try:\n\n" +
                                "• Taking a 30-minute break\n" +
                                "• Going for a walk outside\n" +
                                "• Calling a friend or family member\n" +
                                "• Practicing mindfulness or meditation")
                    } else {
                        append("While your screen time seems moderate, when feeling $emotionLower, try:\n\n" +
                                "• Physical activity or exercise\n" +
                                "• Talking to someone you trust\n" +
                                "• Engaging in a creative hobby\n" +
                                "• Spending time in nature")
                    }
                }
                emotionLower.contains("happy") || emotionLower.contains("joy") -> {
                    append("💡 KEEP IT UP!\n\n" +
                            "Great to see you're feeling positive! To maintain this:\n\n" +
                            "• Continue balanced screen time habits\n" +
                            "• Stay connected with loved ones\n" +
                            "• Keep doing what makes you happy\n" +
                            "• Share positivity with others")
                }
                emotionLower.contains("surprise") -> {
                    append("💡 INTERESTING!\n\n" +
                            "Feeling surprised? That's great! It means you're experiencing new things.\n\n" +
                            "• Use this energy productively\n" +
                            "• Learn something new\n" +
                            "• Stay curious and open-minded")
                }
                else -> {
                    append("💡 DIGITAL BALANCE TIP:\n\n" +
                            "To maintain good digital wellbeing:\n\n" +
                            "• Take regular breaks (20-20-20 rule)\n" +
                            "• Set app time limits\n" +
                            "• Practice mindful usage\n" +
                            "• Prioritize face-to-face connections")
                }
            }
        }
        
        return correlation
    }

    /**
     * Gets emoji for emotion
     */
    private fun getEmotionEmoji(emotion: String): String {
        return when (emotion.lowercase()) {
            "joy", "happy" -> "😊"
            "sadness", "sad" -> "😢"
            "anger", "angry" -> "😠"
            "fear", "anxious" -> "😰"
            "stress", "stressed" -> "😓"
            "neutral" -> "😐"
            else -> "🤔"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
