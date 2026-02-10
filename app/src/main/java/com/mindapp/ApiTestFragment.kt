package com.mindapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment to test API configurations
 * 
 * This helps you verify if your API keys are correctly set up.
 * 
 * HOW TO USE:
 * 1. Add this fragment to your navigation
 * 2. Click "Test Gemini API" and "Test Hugging Face API" buttons
 * 3. Check the results
 */
class ApiTestFragment : Fragment() {

    private lateinit var btnTestGemini: Button
    private lateinit var btnTestHuggingFace: Button
    private lateinit var btnTestBoth: Button
    private lateinit var tvResults: TextView
    private lateinit var scrollView: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create layout programmatically for quick testing
        val context = requireContext()
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // Title
        layout.addView(TextView(context).apply {
            text = "API Configuration Tester"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        })

        // Current config info
        layout.addView(TextView(context).apply {
            text = "Current Configuration:\n\n" +
                    "Gemini Key: ${ApiConfig.GEMINI_API_KEY.take(20)}...\n" +
                    "Is Configured: ${ApiConfig.isGeminiConfigured()}\n\n" +
                    "Hugging Face Key: ${ApiConfig.HUGGING_FACE_API_KEY.take(20)}...\n" +
                    "Is Configured: ${ApiConfig.isHuggingFaceConfigured()}"
            textSize = 14f
            setPadding(0, 0, 0, 32)
        })

        // Test Gemini button
        btnTestGemini = Button(context).apply {
            text = "Test Gemini API"
            setOnClickListener { testGemini() }
        }
        layout.addView(btnTestGemini)

        // Test Hugging Face button
        btnTestHuggingFace = Button(context).apply {
            text = "Test Hugging Face API"
            setOnClickListener { testHuggingFace() }
        }
        layout.addView(btnTestHuggingFace)

        // Test Both button
        btnTestBoth = Button(context).apply {
            text = "Test Both APIs"
            setOnClickListener { testBoth() }
        }
        layout.addView(btnTestBoth)

        // Results text view
        tvResults = TextView(context).apply {
            text = "Click a button to test APIs"
            textSize = 14f
            setPadding(0, 32, 0, 0)
            setTextIsSelectable(true)
        }

        scrollView = ScrollView(context).apply {
            addView(tvResults)
        }
        layout.addView(scrollView)

        return layout
    }

    private fun testGemini() {
        tvResults.text = "Testing Gemini API...\n\nPlease wait..."
        setButtonsEnabled(false)

        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiTester.testGeminiApi(ApiConfig.GEMINI_API_KEY)
            tvResults.text = "=== GEMINI API TEST ===\n\n${result.second}"
            setButtonsEnabled(true)
        }
    }

    private fun testHuggingFace() {
        tvResults.text = "Testing Hugging Face API...\n\nPlease wait..."
        setButtonsEnabled(false)

        CoroutineScope(Dispatchers.Main).launch {
            val result = ApiTester.testHuggingFaceApi(ApiConfig.HUGGING_FACE_API_KEY)
            tvResults.text = "=== HUGGING FACE API TEST ===\n\n${result.second}"
            setButtonsEnabled(true)
        }
    }

    private fun testBoth() {
        tvResults.text = "Testing both APIs...\n\nPlease wait..."
        setButtonsEnabled(false)

        CoroutineScope(Dispatchers.Main).launch {
            val geminiResult = ApiTester.testGeminiApi(ApiConfig.GEMINI_API_KEY)
            val hfResult = ApiTester.testHuggingFaceApi(ApiConfig.HUGGING_FACE_API_KEY)
            
            tvResults.text = """
                === GEMINI API TEST ===
                ${geminiResult.second}
                
                
                === HUGGING FACE API TEST ===
                ${hfResult.second}
            """.trimIndent()
            
            setButtonsEnabled(true)
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        btnTestGemini.isEnabled = enabled
        btnTestHuggingFace.isEnabled = enabled
        btnTestBoth.isEnabled = enabled
    }
}
