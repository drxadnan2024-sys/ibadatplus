package com.ibadatplus.app.alarm.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.ibadatplus.app.MainActivity
import com.ibadatplus.app.alarm.PrayerAlarmPreferences
import com.ibadatplus.app.alarm.PrayerAlarmScheduler
import com.ibadatplus.app.alarm.autosilent.AutoSilentManager
import com.ibadatplus.app.data.model.AlarmToneType
import com.ibadatplus.app.data.model.PrayerName

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRAYER_ALARM = "com.ibadatplus.app.ACTION_PRAYER_ALARM"
        const val CHANNEL_ID = "ibadat_prayer_alarm_channel_high"
        const val CHANNEL_NAME = "Prayer Times & Azan Alarms"

        // Active ringtone reference so DismissReceiver can stop it
        var activeRingtone: Ringtone? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerNameString = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: return
        val toneTypeString = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_TONE_TYPE) ?: AlarmToneType.AZAN.name
        val minutesBefore = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_MINUTES_BEFORE, 0)
        val isPreFajr = intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_IS_PRE_FAJR, false)

        val prayerName = try {
            PrayerName.valueOf(prayerNameString)
        } catch (e: Exception) {
            PrayerName.FAJR
        }

        val toneType = try {
            AlarmToneType.valueOf(toneTypeString)
        } catch (e: Exception) {
            AlarmToneType.AZAN
        }

        // 1. Play Sound / Vibration
        handleSoundAndVibration(context, toneType)

        // 2. Show High-Priority Heads-up Notification with Stop & Snooze
        showPrayerNotification(context, prayerName, minutesBefore, toneType, isPreFajr)

        // 3. Auto-Silent (Masjid Mode): If enabled and this is exact prayer time, mute the phone
        val prefs = PrayerAlarmPreferences(context)
        val autoSilentConfig = prefs.getAutoSilentConfig()
        if (autoSilentConfig.isEnabled && !isPreFajr && minutesBefore == 0) {
            AutoSilentManager.activateSilentMode(
                context = context,
                durationMinutes = autoSilentConfig.durationMinutes,
                mode = autoSilentConfig.silentMode
            )
        }

        // 4. Automatically reschedule for next occurrence
        if (isPreFajr) {
            val preFajrConfig = prefs.getPreFajrConfig()
            if (preFajrConfig.isEnabled) {
                PrayerAlarmScheduler.schedulePreFajrAlarm(context, preFajrConfig)
            }
        } else {
            rescheduleForNextDay(context, prayerName)
        }
    }

    private fun handleSoundAndVibration(context: Context, toneType: AlarmToneType) {
        when (toneType) {
            AlarmToneType.AZAN -> {
                playAlarmSound(context, RingtoneManager.TYPE_ALARM)
                triggerVibration(context)
            }
            AlarmToneType.NORMAL_BEEP -> {
                playAlarmSound(context, RingtoneManager.TYPE_NOTIFICATION)
                triggerVibration(context)
            }
            AlarmToneType.VIBRATION_ONLY -> {
                triggerVibration(context)
            }
        }
    }

    private fun playAlarmSound(context: Context, ringtoneType: Int) {
        try {
            activeRingtone?.stop()
            val uri: Uri = RingtoneManager.getDefaultUri(ringtoneType)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.play()
            activeRingtone = ringtone
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerVibration(context: Context) {
        try {
            val pattern = longArrayOf(0, 600, 300, 600, 300, 900)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showPrayerNotification(
        context: Context,
        prayerName: PrayerName,
        minutesBefore: Int,
        toneType: AlarmToneType,
        isPreFajr: Boolean
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundUri = RingtoneManager.getDefaultUri(
            if (toneType == AlarmToneType.AZAN) RingtoneManager.TYPE_ALARM else RingtoneManager.TYPE_NOTIFICATION
        )

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Islamic daily prayer notifications, Azan & Masjid mode alarms"
                enableVibration(true)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentTitle = when {
            isPreFajr -> "🌙 Tahajjud / Sehri Reminder (${minutesBefore}m before Fajr)"
            minutesBefore > 0 -> "${prayerName.displayName} Prayer in $minutesBefore Minutes"
            else -> "🕌 Time for ${prayerName.displayName} Prayer (${prayerName.arabicName})"
        }

        val contentText = when {
            isPreFajr -> "Wake up for Tahajjud prayers or Sehri before Fajr begins."
            else -> "Hayya 'ala-s-Salah - Come to prayer. It is time for ${prayerName.displayName}."
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            prayerName.ordinal,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = if (isPreFajr) 7000 else (prayerName.ordinal + 5000)

        // 1. "Stop" Action Button
        val stopIntent = Intent(context, AlarmDismissReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
            putExtra(AlarmDismissReceiver.EXTRA_SNOOZE, false)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            prayerName.ordinal + 200,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. "Snooze (10m)" Action Button
        val snoozeIntent = Intent(context, AlarmDismissReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
            putExtra(AlarmDismissReceiver.EXTRA_SNOOZE, true)
            putExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(PrayerAlarmScheduler.EXTRA_TONE_TYPE, toneType.name)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            prayerName.ordinal + 300,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 10m", snoozePendingIntent)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun rescheduleForNextDay(context: Context, prayerName: PrayerName) {
        val prefs = PrayerAlarmPreferences(context)
        val config = prefs.getAlarmConfig(prayerName)
        if (config.isEnabled) {
            PrayerAlarmScheduler.scheduleAlarm(context, prayerName, config)
        }
    }
}
