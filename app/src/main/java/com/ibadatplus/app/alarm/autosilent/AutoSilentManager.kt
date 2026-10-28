package com.ibadatplus.app.alarm.autosilent

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.ibadatplus.app.alarm.PrayerAlarmPreferences
import com.ibadatplus.app.data.model.AutoSilentMode

object AutoSilentManager {

    private const val PREF_PREVIOUS_RINGER_MODE = "saved_previous_ringer_mode"
    private const val NOTIFICATION_ID = 8888
    private const val CHANNEL_ID = "ibadat_auto_silent_channel"

    const val ACTION_START_AUTO_SILENT = "com.ibadatplus.app.ACTION_START_AUTO_SILENT"
    const val ACTION_RESTORE_RINGER = "com.ibadatplus.app.ACTION_RESTORE_RINGER"
    const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
    const val EXTRA_SILENT_MODE = "extra_silent_mode"

    fun isNotificationPolicyAccessGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    fun createDndSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    /**
     * Activates Masjid Mode (Vibrate or Silent/DND) and schedules ringer restoration
     */
    fun activateSilentMode(context: Context, durationMinutes: Int, mode: AutoSilentMode) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val prefs = context.getSharedPreferences("auto_silent_state", Context.MODE_PRIVATE)

        // Save current ringer mode before altering
        val currentRinger = audioManager.ringerMode
        prefs.edit().putInt(PREF_PREVIOUS_RINGER_MODE, currentRinger).apply()

        try {
            if (mode == AutoSilentMode.DND_SILENT && isNotificationPolicyAccessGranted(context)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
                }
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            } else {
                // Safe Vibrate Mode (works everywhere)
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }

            // Post notification indicating Masjid Mode is active
            showActiveNotification(context, durationMinutes)

            // Schedule ringer restore after durationMinutes
            scheduleRestoreAlarm(context, durationMinutes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Restores previous normal ringer mode
     */
    fun restoreNormalRingerMode(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val prefs = context.getSharedPreferences("auto_silent_state", Context.MODE_PRIVATE)

        val previousRinger = prefs.getInt(PREF_PREVIOUS_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNotificationPolicyAccessGranted(context)) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
            audioManager.ringerMode = previousRinger

            // Cancel notification
            notificationManager.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleRestoreAlarm(context: Context, durationMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)

        val intent = Intent(context, AutoSilentReceiver::class.java).apply {
            action = ACTION_RESTORE_RINGER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun showActiveNotification(context: Context, durationMinutes: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Masjid Auto-Silent Mode",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active Masjid mode status during congregational prayers"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val restoreIntent = Intent(context, AutoSilentReceiver::class.java).apply {
            action = ACTION_RESTORE_RINGER
        }
        val restorePendingIntent = PendingIntent.getBroadcast(
            context,
            9998,
            restoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Masjid Mode Active (Phone Silent)")
            .setContentText("Phone will automatically unmute in $durationMinutes min after Salah.")
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_revert, "Unmute Now", restorePendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
