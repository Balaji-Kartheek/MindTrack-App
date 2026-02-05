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
    
    private lateinit var geminiService: GeminiService
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

        setupRetrofit()
        setupRecyclerView()
        
        // Add welcome message
        addWelcomeMessage()
        
        // Set up send button
        binding.btnSend.setOnClickListener {
            sendMessage()
        }
        
        // Allow sending on Enter key
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    /**
     * Sets up Retrofit for API calls
     */
    private fun setupRetrofit() {
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
        val welcomeMessage = ChatMessage(
            text = "Hello! I'm your Digital Wellbeing Assistant. " +
                    "I can help you understand your app usage patterns and provide tips " +
                    "for healthier digital habits. How can I help you today?",
            isUser = false
        )
        chatMessages.add(welcomeMessage)
        chatAdapter.submitList(chatMessages.toList())
    }

    /**
     * Sends user message and gets AI response
     */
    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isEmpty()) return

        // Check API key
        if (ApiConfig.GEMINI_API_KEY == "YOUR_GEMINI_API_KEY_HERE") {
            Toast.makeText(
                requireContext(),
                "Please configure your Gemini API key in ApiConfig.kt",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Add user message
        val userMessage = ChatMessage(text = messageText, isUser = true)
        chatMessages.add(userMessage)
        chatAdapter.submitList(chatMessages.toList())
        binding.etMessage.text?.clear()

        // Show loading indicator
        val loadingMessage = ChatMessage(text = "Thinking...", isUser = false, isLoading = true)
        chatMessages.add(loadingMessage)
        chatAdapter.submitList(chatMessages.toList())
        scrollToBottom()

        // Get usage stats context for better responses
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usageContext = getUsageContext()
                val enhancedPrompt = buildPrompt(messageText, usageContext)

                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = enhancedPrompt))
                        )
                    )
                )

                val response = geminiService.generateContent(request = request)

                withContext(Dispatchers.Main) {
                    // Remove loading message
                    chatMessages.removeAt(chatMessages.size - 1)

                    if (response.isSuccessful && response.body()?.candidates?.isNotEmpty() == true) {
                        val aiResponse = response.body()!!.candidates[0].content.parts[0].text
                        val aiMessage = ChatMessage(text = aiResponse, isUser = false)
                        chatMessages.add(aiMessage)
                    } else {
                        val errorMessage = ChatMessage(
                            text = "Sorry, I couldn't process your request. " +
                                    "Please check your API key and try again.",
                            isUser = false
                        )
                        chatMessages.add(errorMessage)
                    }

                    chatAdapter.submitList(chatMessages.toList())
                    scrollToBottom()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Remove loading message
                    if (chatMessages.isNotEmpty() && chatMessages.last().isLoading) {
                        chatMessages.removeAt(chatMessages.size - 1)
                    }

                    val errorMessage = ChatMessage(
                        text = "Error: ${e.message}. Please check your internet connection and API key.",
                        isUser = false
                    )
                    chatMessages.add(errorMessage)
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
        return withContext(Dispatchers.IO) {
            try {
                if (UsageStatsHelper.hasUsageStatsPermission(requireContext())) {
                    val totalTime = UsageStatsHelper.getTotalScreenTime(requireContext())
                    val socialMediaTime = UsageStatsHelper.getSocialMediaUsage(requireContext())
                    val topApps = UsageStatsHelper.getTopApps(requireContext(), 3)
                    
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
        binding.recyclerViewMessages.post {
            val itemCount = chatAdapter.itemCount
            if (itemCount > 0) {
                binding.recyclerViewMessages.smoothScrollToPosition(itemCount - 1)
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
