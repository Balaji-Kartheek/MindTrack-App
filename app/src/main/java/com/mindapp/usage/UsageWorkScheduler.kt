package com.mindapp.usage

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules periodic checks for heavy other-apps usage (WorkManager minimum interval: 15 minutes).
 */
object UsageWorkScheduler {
    private const val UNIQUE_NAME = "mindapp_usage_threshold"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<UsageThresholdWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
