package com.mindapp.usage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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

        val threshold = MindAppPrefs.getUsageThresholdMs(ctx)
        val totalOtherApps = UsageStatsHelper.getTotalScreenTime(ctx)
        if (totalOtherApps < threshold) {
            return Result.success()
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val prefs = MindAppPrefs.prefs(ctx)
        if (prefs.getString(MindAppPrefs.KEY_HEAVY_USAGE_NOTIFIED_DATE, null) == today) {
            return Result.success()
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

        prefs.edit().putString(MindAppPrefs.KEY_HEAVY_USAGE_NOTIFIED_DATE, today).apply()
        return Result.success()
    }
}
