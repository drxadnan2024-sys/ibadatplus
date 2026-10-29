package com.ibadatplus.app.alarm.autosilent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ibadatplus.app.alarm.PrayerAlarmScheduler
import com.ibadatplus.app.data.model.AutoSilentMode

class AutoSilentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AutoSilentManager.ACTION_START_AUTO_SILENT -> {
                val duration = intent.getIntExtra(AutoSilentManager.EXTRA_DURATION_MINUTES, 25)
                val modeName = intent.getStringExtra(AutoSilentManager.EXTRA_SILENT_MODE)
                val mode = try {
                    AutoSilentMode.valueOf(modeName ?: AutoSilentMode.VIBRATE.name)
                } catch (e: Exception) {
                    AutoSilentMode.VIBRATE
                }
                AutoSilentManager.activateSilentMode(context, duration, mode)
            }
            AutoSilentManager.ACTION_RESTORE_RINGER -> {
                AutoSilentManager.restoreNormalRingerMode(context)
                PrayerAlarmScheduler.rescheduleAutoSilentAlarms(context)
            }
        }
    }
}
