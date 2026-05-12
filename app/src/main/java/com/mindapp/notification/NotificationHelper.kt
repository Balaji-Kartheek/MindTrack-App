package com.mindapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mindapp.R

/**
 * Helper class for managing notifications
 * 
 * Handles creating notification channels and sending alerts
 * when social media usage exceeds the threshold
 */
object NotificationHelper {
    private const val CHANNEL_ID = "usage_alerts"
    private const val CHANNEL_NAME = "Usage Alerts"
    private const val NOTIFICATION_ID = 1

    private const val CHANNEL_BALANCE = "balance_reminders"
    private const val CHANNEL_BALANCE_NAME = "Balance reminders"
    private const val NOTIFICATION_HEAVY_USAGE = 1002

    /**
     * Creates notification channel (required for Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts for excessive app usage"
                }
                nm?.createNotificationChannel(channel)

                val balance = NotificationChannel(
                    CHANNEL_BALANCE,
                    CHANNEL_BALANCE_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Gentle reminders when other-app time crosses your daily threshold"
                }
                nm?.createNotificationChannel(balance)
            }
        } catch (e: Exception) {
            // Ignore - notifications are non-critical
        }
    }

    /**
     * Sends a notification alert when social media usage exceeds 3 hours
     */
    fun sendSocialMediaAlert(context: Context, usageTime: String) {
        try {
            createNotificationChannel(context)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Social Media Usage Alert")
                .setContentText("You've used social media for $usageTime today. Consider taking a break!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // Don't crash app if notification fails
        }
    }

    /**
     * Fired when today's non–MindApp screen time crosses the user's threshold (background check).
     */
    fun sendHeavyUsageReminder(context: Context, usageFormatted: String, wellnessTip: String) {
        try {
            createNotificationChannel(context)
            val notification = NotificationCompat.Builder(context, CHANNEL_BALANCE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("You’ve been on other apps a lot today")
                .setContentText("About $usageFormatted so far. $wellnessTip")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("About $usageFormatted on other apps today.\n\n$wellnessTip"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(NOTIFICATION_HEAVY_USAGE, notification)
        } catch (e: Exception) {
            // Non-critical
        }
    }
}
