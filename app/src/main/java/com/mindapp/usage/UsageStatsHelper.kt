package com.mindapp.usage

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import java.util.Calendar

/**
 * Helper class for managing app usage statistics
 * 
 * This class uses Android's UsageStatsManager API to track app usage
 * and categorize apps into different types (Social Media, Productivity, etc.)
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
        "org.telegram.messenger",
        "com.reddit.frontpage",
        "com.google.android.youtube"
    )

    /**
     * Checks if Usage Stats permission is granted.
     * Returns false on any error to avoid crashes on devices with different APIs.
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
     * Gets usage stats for today. Returns empty list on any error to avoid crashes.
     */
    fun getTodayUsageStats(context: Context): List<AppUsageInfo> {
        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return emptyList()
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()
            
            // Use INTERVAL_BEST for more accurate stats across different devices
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                endTime
            )

            val packageManager = context.packageManager
            val appUsageMap = mutableMapOf<String, AppUsageInfo>()

        // Process stats - some devices return duplicates, so aggregate by package name
        stats?.forEach { usageStat ->
            val packageName = usageStat.packageName
            
            // Skip this app itself
            if (packageName == context.packageName) return@forEach
            
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                
                // Allow both user apps and some system apps (don't skip all system apps)
                // This ensures we track apps like YouTube, WhatsApp, etc.
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                
                // Skip only pure system apps (not updated system apps like YouTube, Chrome, etc.)
                if (isSystemApp && !isUpdatedSystemApp) {
                    // Still allow social media system apps
                    if (!SOCIAL_MEDIA_PACKAGES.contains(packageName)) {
                        return@forEach
                    }
                }
                
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val totalTime = usageStat.totalTimeInForeground
                
                if (totalTime > 0) {
                    val existing = appUsageMap[packageName]
                    if (existing != null) {
                        // Create new instance with updated totalTime (data class is immutable)
                        appUsageMap[packageName] = existing.copy(
                            totalTime = existing.totalTime + totalTime
                        )
                    } else {
                        val category = getAppCategory(packageName)
                        appUsageMap[packageName] = AppUsageInfo(
                            packageName = packageName,
                            appName = appName,
                            totalTime = totalTime,
                            category = category
                        )
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // App not found, skip
            } catch (e: Exception) {
                // Skip on any error for this app
            }
        }

            appUsageMap.values.sortedByDescending { it.totalTime }
        } catch (e: Exception) {
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
