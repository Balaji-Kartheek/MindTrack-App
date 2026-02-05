package com.mindapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mindapp.chatbot.ChatbotFragment
import com.mindapp.mood.MoodCheckFragment
import com.mindapp.usage.UsageStatsFragment
import com.mindapp.usage.UsageStatsHelper

/**
 * Main Activity with Bottom Navigation
 * 
 * This activity hosts three main fragments:
 * 1. UsageStatsFragment - Shows app usage statistics
 * 2. ChatbotFragment - AI chatbot powered by Gemini
 * 3. MoodCheckFragment - Emotion detection and mood tracking
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Set up bottom navigation listener
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_usage -> {
                    loadFragment(UsageStatsFragment())
                    true
                }
                R.id.nav_chat -> {
                    loadFragment(ChatbotFragment())
                    true
                }
                R.id.nav_mood -> {
                    loadFragment(MoodCheckFragment())
                    true
                }
                else -> false
            }
        }

        // Load default fragment (Usage Stats)
        if (savedInstanceState == null) {
            loadFragment(UsageStatsFragment())
            bottomNavigation.selectedItemId = R.id.nav_usage
        }

        // Check and request Usage Stats permission
        checkUsageStatsPermission()
    }

    /**
     * Loads a fragment into the container
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /**
     * Checks if Usage Stats permission is granted
     * If not, shows a dialog to guide user to settings
     */
    private fun checkUsageStatsPermission() {
        if (!UsageStatsHelper.hasUsageStatsPermission(this)) {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs Usage Stats permission to track your app usage. " +
                        "Please grant this permission in the settings.")
                .setPositiveButton("Open Settings") { _, _ ->
                    openUsageStatsSettings()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(this, "Permission is required for the app to work", 
                        Toast.LENGTH_LONG).show()
                }
                .setCancelable(false)
                .show()
        }
    }

    /**
     * Opens the Usage Stats settings screen
     */
    private fun openUsageStatsSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }
}
