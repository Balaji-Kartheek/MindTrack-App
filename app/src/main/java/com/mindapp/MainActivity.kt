package com.mindapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mindapp.chatbot.ChatbotFragment
import com.mindapp.mood.MoodCheckFragment
import com.mindapp.usage.UsageStatsFragment
import com.mindapp.usage.UsageStatsHelper
import com.mindapp.usage.UsageWorkScheduler
import com.mindapp.wellness.WellnessFragment

/**
 * Main Activity with Bottom Navigation
 * 
 * This activity hosts four main fragments:
 * Usage, Chat, Mood, and Wellness (balance tips + threshold).
 */
class MainActivity : AppCompatActivity() {

    private var bottomNavigation: BottomNavigationView? = null

    companion object {
        private const val RC_POST_NOTIFICATIONS = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install crash handler to show message instead of silent crash
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Log.e("MindApp", "Uncaught exception", throwable)
            runOnUiThread {
                try {
                    Toast.makeText(
                        this@MainActivity,
                        "Something went wrong. Please try again.\n${throwable.message?.take(50) ?: ""}",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (_: Exception) { }
            }
        }
        
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            bottomNavigation = findViewById(R.id.bottom_navigation)
            val nav = bottomNavigation ?: return

            UsageWorkScheduler.schedule(this)
            requestPostNotificationsIfNeeded()

            // Set up bottom navigation listener
            nav.setOnItemSelectedListener { item ->
                try {
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
                        R.id.nav_wellness -> {
                            loadFragment(WellnessFragment())
                            true
                        }
                        else -> false
                    }
                } catch (e: Exception) {
                    Log.e("MindApp", "Navigation error", e)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    false
                }
            }

            // Load default fragment (Usage Stats)
            if (savedInstanceState == null) {
                loadFragment(UsageStatsFragment())
                nav.selectedItemId = R.id.nav_usage
            }

            // Check and request Usage Stats permission (non-blocking)
            try {
                checkUsageStatsPermission()
            } catch (e: Exception) {
                Log.e("MindApp", "Permission check error", e)
                Toast.makeText(this, "Could not check permission. You can enable it in Settings.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("MindApp", "onCreate failed", e)
            Toast.makeText(this, "App failed to start: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /**
     * Loads a fragment into the container
     */
    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } catch (e: Exception) {
            Log.e("MindApp", "loadFragment failed", e)
        }
    }

    /**
     * Checks if Usage Stats permission is granted
     * If not, shows a dialog to guide user to settings
     */
    private fun checkUsageStatsPermission() {
        try {
            if (!UsageStatsHelper.hasUsageStatsPermission(this)) {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs Usage Stats permission to track your app usage. " +
                            "Please grant this permission in the settings.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        try { openUsageStatsSettings() } catch (_: Exception) { }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        Toast.makeText(this, "You can enable Usage access later in Settings.", Toast.LENGTH_LONG).show()
                    }
                    .setCancelable(true)
                    .show()
            }
        } catch (e: Exception) {
            Log.e("MindApp", "checkUsageStatsPermission", e)
        }
    }

    /**
     * Opens the Usage Stats settings screen
     */
    private fun openUsageStatsSettings() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MindApp", "openUsageStatsSettings", e)
            Toast.makeText(this, "Could not open Settings.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPostNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            RC_POST_NOTIFICATIONS
        )
    }
}
