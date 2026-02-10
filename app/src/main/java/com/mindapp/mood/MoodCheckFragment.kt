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
            Toast.makeText(requireContext(), "Please enter how you're feeling", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ApiConfig.isHuggingFaceConfigured()) {
            Toast.makeText(
                requireContext(),
                "⚠️ API key not configured! You need to:\n1. Set GitHub Secrets\n2. Build NEW APK\n3. Install NEW APK\nSee GITHUB_SECRETS_SETUP.md",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val service = emotionService
        if (service == null) {
            Toast.makeText(requireContext(), "Mood service not available. Please restart the app.", Toast.LENGTH_SHORT).show()
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
                        if (emotions != null) {
                            displayEmotionResults(emotions, inputText)
                            correlateWithUsage(emotions)
                        } else {
                            if (isAdded) Toast.makeText(requireContext(), "Error: No response from API", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        if (isAdded) Toast.makeText(requireContext(), "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _binding?.progressBar?.visibility = View.GONE
                    _binding?.btnAnalyze?.isEnabled = true
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error: ${e.message}. Check internet and API key.", Toast.LENGTH_LONG).show()
                    }
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
            append("Detected Emotion: ${topEmotion.label.uppercase()}\n\n")
            append("Confidence Scores:\n")
            sortedEmotions.forEach { emotion ->
                val percentage = (emotion.score * 100).toInt()
                append("• ${emotion.label}: $percentage%\n")
            }
        }
        _binding?.tvEmotionResult?.text = emotionText
        _binding?.tvEmotionResult?.visibility = View.VISIBLE
        val emoji = getEmotionEmoji(topEmotion.label)
        _binding?.tvEmotionEmoji?.text = emoji
        _binding?.tvEmotionEmoji?.visibility = View.VISIBLE
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
            append("📊 Usage Correlation:\n\n")
            append("You seem ${emotionLower} and your usage today:\n")
            append("• Total screen time: $totalTimeFormatted\n")
            append("• Social media: $socialMediaTimeFormatted\n")
            
            if (topApps.isNotEmpty()) {
                append("• Top apps: ${topApps.joinToString(", ") { it.appName }}\n")
            }
            
            append("\n")
            
            // Add insights based on emotion
            when {
                emotionLower.contains("sad") || emotionLower.contains("anxious") || 
                emotionLower.contains("stressed") -> {
                    val threeHours = 3 * 60 * 60 * 1000L
                    if (socialMediaTime > threeHours) {
                        append("💡 Insight: High social media usage ($socialMediaTimeFormatted) " +
                                "might be contributing to your ${emotionLower} feelings. " +
                                "Consider taking breaks and limiting social media time.")
                    } else {
                        append("💡 Insight: Consider engaging in activities that boost your mood, " +
                                "like exercise, hobbies, or connecting with friends offline.")
                    }
                }
                emotionLower.contains("happy") || emotionLower.contains("joy") -> {
                    append("💡 Insight: Great to see you're feeling positive! " +
                            "Keep maintaining a healthy balance between screen time and real-world activities.")
                }
                else -> {
                    append("💡 Insight: Monitor your screen time patterns and ensure " +
                            "you're taking regular breaks from digital devices.")
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
