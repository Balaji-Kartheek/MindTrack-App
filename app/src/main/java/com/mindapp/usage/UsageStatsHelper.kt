package com.mindapp.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.util.Calendar

/**
 * Helper class for managing app usage statistics
 * 
 * Uses queryEvents() (like Digital Wellbeing) instead of queryUsageStats()
 * to get accurate real-time foreground usage data.
 * 
 * How it works:
 * - queryEvents() returns individual MOVE_TO_FOREGROUND / MOVE_TO_BACKGROUND events
 * - We calculate each app's foreground time by pairing these events
 * - This gives the same real-time accuracy as Android's Digital Wellbeing screen
 */
object UsageStatsHelper {

    // Social media apps to track
    private val SOCIAL_MEDIA_PACKAGES = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.twitter.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.snapchat.android",
        "com.whatsapp",
        "com.whatsapp.w4b", // WhatsApp Business
        "org.telegram.messenger",
        "com.reddit.frontpage",
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music",
        "com.twitter.android.lite",
        "com.facebook.lite",
        "com.facebook.mlite" // Facebook Lite/Messenger Lite
    )

    // System packages to always exclude (launchers, OS internals)
    private val EXCLUDED_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.android.launcher",
        "com.android.launcher3",
        "com.google.android.permissioncontroller",
        "com.android.providers.settings"
    )

    /**
     * Checks if Usage Stats permission is granted.
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager
                ?: return false
            val mode = appOpsManager.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets usage stats for today using queryEvents() for real-time accuracy.
     * 
     * KEY CONCEPT (learn this!):
     * - queryUsageStats() returns cached/aggregated data → often stale, not real-time
     * - queryEvents() returns raw FOREGROUND/BACKGROUND events → always accurate
     * - Digital Wellbeing on your phone uses this same events-based approach
     * 
     * We track each app's foreground time by:
     * 1. Recording when an app moves to foreground (start timestamp)
     * 2. When it moves to background, calculate duration = background_time - foreground_time
     * 3. Sum all durations per app
     */
    fun getTodayUsageStats(context: Context): List<AppUsageInfo> {
        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return emptyList()
            
            // Query from midnight to now
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()
            
            // queryEvents gives us individual FOREGROUND/BACKGROUND events - real-time!
            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            
            // Track foreground start times per package
            val foregroundStartMap = mutableMapOf<String, Long>()
            // Accumulate total foreground time per package
            val totalTimeMap = mutableMapOf<String, Long>()
            
            val event = UsageEvents.Event()
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                val pkg = event.packageName ?: continue
                
                when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        // App came to foreground - record the start time
                        foregroundStartMap[pkg] = event.timeStamp
                    }
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        // App went to background - calculate how long it was in foreground
                        val startTs = foregroundStartMap.remove(pkg)
                        if (startTs != null && event.timeStamp > startTs) {
                            val duration = event.timeStamp - startTs
                            totalTimeMap[pkg] = (totalTimeMap[pkg] ?: 0L) + duration
                        }
                    }
                }
            }
            
            // For apps still in foreground (no BACKGROUND event yet), count time until now
            val now = System.currentTimeMillis()
            for ((pkg, startTs) in foregroundStartMap) {
                if (now > startTs) {
                    val duration = now - startTs
                    totalTimeMap[pkg] = (totalTimeMap[pkg] ?: 0L) + duration
                }
            }
            
            android.util.Log.d("UsageStatsHelper", "Events query: ${totalTimeMap.size} apps with foreground time")

            val packageManager = context.packageManager
            val appUsageList = mutableListOf<AppUsageInfo>()

            for ((packageName, totalTime) in totalTimeMap) {
                // Skip our own app
                if (packageName == context.packageName) continue
                // Skip excluded system internals
                if (EXCLUDED_PACKAGES.contains(packageName)) continue
                // Skip apps with less than 1 minute usage
                if (totalTime < 60_000) continue
                
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val category = getAppCategory(packageName)
                    
                    appUsageList.add(AppUsageInfo(
                        packageName = packageName,
                        appName = appName,
                        totalTime = totalTime,
                        category = category
                    ))
                    
                    android.util.Log.d("UsageStatsHelper", "App: $appName ($packageName), Time: ${formatTime(totalTime)}")
                } catch (e: PackageManager.NameNotFoundException) {
                    // App uninstalled or not found, skip
                } catch (e: Exception) {
                    android.util.Log.e("UsageStatsHelper", "Error processing $packageName", e)
                }
            }
            
            android.util.Log.d("UsageStatsHelper", "Final list: ${appUsageList.size} apps")
            appUsageList.sortedByDescending { it.totalTime }
        } catch (e: Exception) {
            android.util.Log.e("UsageStatsHelper", "getTodayUsageStats failed", e)
            emptyList()
        }
    }

    /**
     * Categorizes an app based on its package name
     */
    private fun getAppCategory(packageName: String): AppCategory {
        return when {
            SOCIAL_MEDIA_PACKAGES.contains(packageName) -> AppCategory.SOCIAL_MEDIA
            packageName.contains("office") || 
            packageName.contains("productivity") ||
            packageName.contains("notes") ||
            packageName.contains("calendar") ||
            packageName.contains("gmail") ||
            packageName.contains("drive") -> AppCategory.PRODUCTIVITY
            packageName.contains("game") ||
            packageName.contains("entertainment") ||
            packageName.contains("netflix") ||
            packageName.contains("spotify") ||
            packageName.contains("music") ||
            packageName.contains("video") -> AppCategory.ENTERTAINMENT
            else -> AppCategory.OTHERS
        }
    }

    /**
     * Gets total screen time for today
     */
    fun getTotalScreenTime(context: Context): Long {
        val usageStats = getTodayUsageStats(context)
        return usageStats.sumOf { it.totalTime }
    }

    /**
     * Gets social media usage time for today
     */
    fun getSocialMediaUsage(context: Context): Long {
        val usageStats = getTodayUsageStats(context)
        return usageStats
            .filter { it.category == AppCategory.SOCIAL_MEDIA }
            .sumOf { it.totalTime }
    }

    /**
     * Gets top N most used apps
     */
    fun getTopApps(context: Context, limit: Int = 5): List<AppUsageInfo> {
        return getTodayUsageStats(context).take(limit)
    }

    /**
     * Formats time in milliseconds to readable string (e.g., "2h 30m")
     */
    fun formatTime(timeInMillis: Long): String {
        val totalSeconds = timeInMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${totalSeconds}s"
        }
    }
}

/**
 * Data class representing app usage information
 */
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTime: Long,
    val category: AppCategory
)

/**
 * Enum for app categories
 */
enum class AppCategory {
    SOCIAL_MEDIA,
    PRODUCTIVITY,
    ENTERTAINMENT,
    OTHERS
}
