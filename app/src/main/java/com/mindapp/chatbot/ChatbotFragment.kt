package com.mindapp.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindapp.ApiConfig
import com.mindapp.R
import com.mindapp.databinding.FragmentChatbotBinding
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
 * Fragment for AI Chatbot powered by Google Gemini
 * 
 * Allows users to:
 * - Chat with AI about digital wellbeing
 * - Get tips on reducing screen time
 * - Ask questions about usage patterns
 * - View conversation history
 */
class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!
    
    private var geminiService: GeminiService? = null
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupRetrofit()
            setupRecyclerView()
            addWelcomeMessage()
            binding.btnSend.setOnClickListener { sendMessage() }
            binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage()
                    true
                } else false
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatbotFragment", "onViewCreated", e)
            addWelcomeMessage()
            if (::chatAdapter.isInitialized) {
                chatMessages.add(ChatMessage(text = "Chat is temporarily unavailable. Please check API key in ApiConfig.kt", isUser = false))
                chatAdapter.submitList(chatMessages.toList())
            }
        }
    }

    /**
     * Sets up Retrofit for API calls. Safe to call even if API key is not set.
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
                .baseUrl(ApiConfig.GEMINI_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            geminiService = retrofit.create(GeminiService::class.java)
        } catch (e: Exception) {
            android.util.Log.e("ChatbotFragment", "setupRetrofit failed", e)
            geminiService = null
        }
    }

    /**
     * Sets up RecyclerView for chat messages
     */
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    /**
     * Adds welcome message to chat
     */
    private fun addWelcomeMessage() {
        if (!::chatAdapter.isInitialized) return
        val welcomeText = if (ApiConfig.isGeminiConfigured()) {
            "👋 Hello! I'm your Digital Wellbeing Assistant powered by Google Gemini.\n\n" +
            "I can help you:\n" +
            "• Understand your screen time patterns\n" +
            "• Get tips for healthier digital habits\n" +
            "• Set realistic usage goals\n" +
            "• Improve your digital wellbeing\n\n" +
            "How can I assist you today?"
        } else {
            "⚠️ API Configuration Required\n\n" +
            "To use the AI assistant, please:\n" +
            "1. Add your Gemini API key to local.properties\n" +
            "2. Rebuild the app\n\n" +
            "Meanwhile, check out the Usage and Mood tabs!"
        }
        chatMessages.clear()
        chatMessages.add(ChatMessage(text = welcomeText, isUser = false))
        chatAdapter.submitList(chatMessages.toList())
    }

    /**
     * Sends user message and gets AI response
     */
    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isEmpty()) return

        if (!ApiConfig.isGeminiConfigured()) {
            chatMessages.add(ChatMessage(
                text = "⚠️ Configuration Error\n\nGemini API key is not configured. Please add your API key to local.properties and rebuild the app.",
                isUser = false
            ))
            chatAdapter.submitList(chatMessages.toList())
            scrollToBottom()
            return
        }

        val service = geminiService
        if (service == null) {
            chatMessages.add(ChatMessage(
                text = "❌ Service Error\n\nChat service is not available. Please restart the app.",
                isUser = false
            ))
            chatAdapter.submitList(chatMessages.toList())
            scrollToBottom()
            return
        }

        val userMessage = ChatMessage(text = messageText, isUser = true)
        chatMessages.add(userMessage)
        chatAdapter.submitList(chatMessages.toList())
        binding.etMessage.text?.clear()
        scrollToBottom()

        val loadingMessage = ChatMessage(text = "✨ Thinking...", isUser = false, isLoading = true)
        chatMessages.add(loadingMessage)
        chatAdapter.submitList(chatMessages.toList())
        scrollToBottom()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usageContext = getUsageContext()
                val enhancedPrompt = buildPrompt(messageText, usageContext)
                val request = GeminiRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = enhancedPrompt)))
                    )
                )
                val response = service.generateContent(request = request)

                withContext(Dispatchers.Main) {
                    if (chatMessages.isNotEmpty() && chatMessages.last().isLoading) {
                        chatMessages.removeAt(chatMessages.size - 1)
                    }
                    val responseBody = response.body()
                    val candidates = responseBody?.candidates
                    if (response.isSuccessful && candidates?.isNotEmpty() == true) {
                        val firstCandidate = candidates.firstOrNull()
                        val aiResponse = firstCandidate?.content?.parts?.firstOrNull()?.text
                        if (aiResponse != null) {
                            chatMessages.add(ChatMessage(text = aiResponse, isUser = false))
                        } else {
                            chatMessages.add(ChatMessage(text = "Sorry, I couldn't parse the AI response.", isUser = false))
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                        chatMessages.add(ChatMessage(
                            text = "❌ API Error\n\nCouldn't process your request.\n\nDetails: ${response.code()} - $errorMsg\n\nPlease check your API key in local.properties and rebuild the app.",
                            isUser = false
                        ))
                    }
                    chatAdapter.submitList(chatMessages.toList())
                    scrollToBottom()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (chatMessages.isNotEmpty() && chatMessages.last().isLoading) {
                        chatMessages.removeAt(chatMessages.size - 1)
                    }
                    val errorMsg = when {
                        e.message?.contains("Unable to resolve host") == true -> 
                            "🌐 Network Error\n\nPlease check your internet connection and try again."
                        e.message?.contains("timeout") == true -> 
                            "⏱️ Timeout Error\n\nThe request took too long. Please try again."
                        else -> 
                            "❌ Error\n\n${e.message ?: "Unknown error"}\n\nPlease check your internet connection and API key."
                    }
                    chatMessages.add(ChatMessage(text = errorMsg, isUser = false))
                    chatAdapter.submitList(chatMessages.toList())
                    scrollToBottom()
                }
            }
        }
    }

    /**
     * Gets current usage stats for context
     */
    private suspend fun getUsageContext(): String {
        val ctx = context ?: return "Context not available."
        return withContext(Dispatchers.IO) {
            try {
                if (UsageStatsHelper.hasUsageStatsPermission(ctx)) {
                    val totalTime = UsageStatsHelper.getTotalScreenTime(ctx)
                    val socialMediaTime = UsageStatsHelper.getSocialMediaUsage(ctx)
                    val topApps = UsageStatsHelper.getTopApps(ctx, 3)
                    "Current usage stats: Total screen time: ${UsageStatsHelper.formatTime(totalTime)}, " +
                            "Social media: ${UsageStatsHelper.formatTime(socialMediaTime)}, " +
                            "Top apps: ${topApps.joinToString(", ") { it.appName }}"
                } else {
                    "Usage stats permission not granted."
                }
            } catch (e: Exception) {
                "Unable to fetch usage stats."
            }
        }
    }

    /**
     * Builds enhanced prompt with context
     */
    private fun buildPrompt(userMessage: String, usageContext: String): String {
        return """
            You are a helpful Digital Wellbeing Assistant. Your role is to help users understand 
            their app usage patterns and provide tips for healthier digital habits.
            
            Context about the user's current usage: $usageContext
            
            User's question: $userMessage
            
            Please provide a helpful, friendly, and concise response. Focus on digital wellbeing, 
            screen time management, and healthy tech habits. Keep responses under 200 words.
        """.trimIndent()
    }

    /**
     * Scrolls chat to bottom
     */
    private fun scrollToBottom() {
        _binding?.recyclerViewMessages?.post {
            if (::chatAdapter.isInitialized) {
                val itemCount = chatAdapter.itemCount
                if (itemCount > 0) {
                    _binding?.recyclerViewMessages?.smoothScrollToPosition(itemCount - 1)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Data class for chat messages
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)
