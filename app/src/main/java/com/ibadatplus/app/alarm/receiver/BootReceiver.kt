package com.ibadatplus.app.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ibadatplus.app.alarm.PrayerAlarmScheduler

/**
 * Reschedules all enabled prayer alarms when the device is rebooted,
 * app updated, or system time/timezone changes.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            Intent.ACTION_TIME_CHANGED
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            PrayerAlarmScheduler.rescheduleAllAlarms(context)
        }
    }
}
