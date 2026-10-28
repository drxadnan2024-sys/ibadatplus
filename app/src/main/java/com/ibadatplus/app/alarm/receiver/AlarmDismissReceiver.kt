package com.ibadatplus.app.alarm.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ibadatplus.app.alarm.PrayerAlarmScheduler
import com.ibadatplus.app.data.model.AlarmToneType
import com.ibadatplus.app.data.model.PrayerName

class AlarmDismissReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_SNOOZE = "extra_snooze"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Stop active playing Azan or sound
        try {
            AlarmReceiver.activeRingtone?.stop()
            AlarmReceiver.activeRingtone = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Dismiss notification
        val notificationId = intent.getIntExtra("notification_id", -1)
        if (notificationId != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }

        // 3. Handle Snooze request
        val isSnooze = intent.getBooleanExtra(EXTRA_SNOOZE, false)
        if (isSnooze) {
            val prayerNameStr = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME)
            val toneTypeStr = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_TONE_TYPE)

            if (prayerNameStr != null) {
                val prayerName = try { PrayerName.valueOf(prayerNameStr) } catch (e: Exception) { PrayerName.FAJR }
                val toneType = try { AlarmToneType.valueOf(toneTypeStr ?: AlarmToneType.AZAN.name) } catch (e: Exception) { AlarmToneType.AZAN }
                PrayerAlarmScheduler.scheduleSnooze(context, prayerName, toneType, 10)
            }
        }
    }
}
