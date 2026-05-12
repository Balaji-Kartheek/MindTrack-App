package com.mindapp.usage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-enqueues periodic usage checks after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        UsageWorkScheduler.schedule(context)
    }
}
