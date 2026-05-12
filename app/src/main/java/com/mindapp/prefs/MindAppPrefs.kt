package com.mindapp.prefs

import android.content.Context

/**
 * Shared preferences for balance reminders and configurable usage threshold.
 */
object MindAppPrefs {
    private const val PREFS = "mindapp_balance"
    const val KEY_USAGE_THRESHOLD_MS = "usage_threshold_ms"
    const val KEY_HEAVY_USAGE_NOTIFIED_DATE = "heavy_usage_notified_date"
    /** Debug APK: throttle repeat notifications (wall clock). */
    const val KEY_LAST_HEAVY_USAGE_NOTIFY_WALL_MS = "last_heavy_usage_notify_wall_ms"

    /** Default: 1 hour of other-apps screen time today. */
    const val DEFAULT_THRESHOLD_MS = 60L * 60L * 1000L

    fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getUsageThresholdMs(context: Context): Long {
        val v = prefs(context).getLong(KEY_USAGE_THRESHOLD_MS, DEFAULT_THRESHOLD_MS)
        return if (v >= 60_000L) v else DEFAULT_THRESHOLD_MS
    }

    fun setUsageThresholdMs(context: Context, ms: Long) {
        prefs(context).edit().putLong(KEY_USAGE_THRESHOLD_MS, ms.coerceAtLeast(60_000L)).apply()
    }
}
