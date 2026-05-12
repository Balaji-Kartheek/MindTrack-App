package com.mindapp.usage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindapp.BuildConfig
import com.mindapp.notification.NotificationHelper
import com.mindapp.prefs.MindAppPrefs
import com.mindapp.wellness.WellnessTips
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsageThresholdWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (!UsageStatsHelper.hasUsageStatsPermission(ctx)) {
            return Result.success()
        }

        // Debug APK (GitHub artifact): 3 min threshold + 5 min cooldown for easier notification testing.
        val threshold = if (BuildConfig.DEBUG) {
            3L * 60L * 1000L
        } else {
            MindAppPrefs.getUsageThresholdMs(ctx)
        }
        val totalOtherApps = UsageStatsHelper.getTotalScreenTime(ctx)
        if (totalOtherApps < threshold) {
            return Result.success()
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val prefs = MindAppPrefs.prefs(ctx)
        if (BuildConfig.DEBUG) {
            val last = prefs.getLong(MindAppPrefs.KEY_LAST_HEAVY_USAGE_NOTIFY_WALL_MS, 0L)
            val cooldownMs = 5L * 60L * 1000L
            if (System.currentTimeMillis() - last < cooldownMs) {
                return Result.success()
            }
        } else {
            if (prefs.getString(MindAppPrefs.KEY_HEAVY_USAGE_NOTIFIED_DATE, null) == today) {
                return Result.success()
            }
        }

        val social = UsageStatsHelper.getSocialMediaUsage(ctx)
        val top = UsageStatsHelper.getTopApps(ctx, 1).firstOrNull()
        val tip = WellnessTips.pickTipForNotification(
            totalOtherMs = totalOtherApps,
            socialMediaMs = social,
            topAppName = top?.appName
        )

        NotificationHelper.sendHeavyUsageReminder(
            ctx,
            UsageStatsHelper.formatTime(totalOtherApps),
            tip
        )

        if (BuildConfig.DEBUG) {
            prefs.edit()
                .putLong(MindAppPrefs.KEY_LAST_HEAVY_USAGE_NOTIFY_WALL_MS, System.currentTimeMillis())
                .apply()
        } else {
            prefs.edit().putString(MindAppPrefs.KEY_HEAVY_USAGE_NOTIFIED_DATE, today).apply()
        }
        return Result.success()
    }
}
