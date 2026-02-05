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

    /**
     * Creates notification channel (required for Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts for excessive app usage"
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.createNotificationChannel(channel)
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
}
