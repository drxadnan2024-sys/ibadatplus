package com.ibadatplus.app.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.ibadatplus.app.alarm.receiver.AlarmReceiver
import com.ibadatplus.app.data.model.AlarmToneType
import com.ibadatplus.app.data.model.PrayerAlarmConfig
import com.ibadatplus.app.data.model.PrayerName
import com.ibadatplus.app.data.model.PreFajrAlarmConfig
import com.ibadatplus.app.data.preferences.CityPreferences
import com.ibadatplus.app.data.repository.PrayerTimeRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object PrayerAlarmScheduler {

    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_TONE_TYPE = "extra_tone_type"
    const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
    const val EXTRA_IS_PRE_FAJR = "extra_is_pre_fajr"

    const val ACTION_PRE_FAJR_ALARM = "com.ibadatplus.app.ACTION_PRE_FAJR_ALARM"

    private fun getRequestCode(prayerName: PrayerName): Int {
        return when (prayerName) {
            PrayerName.FAJR -> 1001
            PrayerName.SUNRISE -> 1002
            PrayerName.DHUHR -> 1003
            PrayerName.ASR -> 1004
            PrayerName.MAGHRIB -> 1005
            PrayerName.ISHA -> 1006
        }
    }

    private const val REQUEST_CODE_PRE_FAJR = 1010
    private const val REQUEST_CODE_SNOOZE = 1020

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun createExactAlarmSettingIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }

    @SuppressLint("BatteryLife")
    fun createBatteryOptimizationIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Schedule exact alarm for a single prayer (Doze mode safe via setExactAndAllowWhileIdle)
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(
        context: Context,
        prayerName: PrayerName,
        config: PrayerAlarmConfig
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!config.isEnabled) {
            cancelAlarm(context, prayerName)
            return
        }

        val repository = PrayerTimeRepository()
        val cityPrefs = CityPreferences(context)
        val defaultCity = cityPrefs.getSavedCity()
        val calcMethod = cityPrefs.getSavedCalculationMethod()
        val asrMadhab = cityPrefs.getSavedAsrMadhab()
        val now = LocalDateTime.now()

        val todaySchedule = repository.getPrayerSchedule(
            date = LocalDate.now(),
            currentTime = LocalTime.now(),
            latitude = defaultCity.latitude,
            longitude = defaultCity.longitude,
            timeZoneId = defaultCity.timeZoneId,
            locationName = defaultCity.name,
            method = calcMethod,
            madhab = asrMadhab
        )

        val prayerItem = todaySchedule.prayers.firstOrNull { it.name == prayerName } ?: return
        var triggerDateTime = todaySchedule.date.atTime(prayerItem.time).minusMinutes(config.minutesBefore.toLong())

        if (triggerDateTime.isBefore(now)) {
            val tomorrow = LocalDate.now().plusDays(1)
            val tomorrowSchedule = repository.getPrayerSchedule(
                date = tomorrow,
                currentTime = LocalTime.MIN,
                latitude = defaultCity.latitude,
                longitude = defaultCity.longitude,
                timeZoneId = defaultCity.timeZoneId,
                locationName = defaultCity.name,
                method = calcMethod,
                madhab = asrMadhab
            )
            val tomorrowPrayer = tomorrowSchedule.prayers.firstOrNull { it.name == prayerName } ?: return
            triggerDateTime = tomorrow.atTime(tomorrowPrayer.time).minusMinutes(config.minutesBefore.toLong())
        }

        val triggerEpochMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(EXTRA_TONE_TYPE, config.toneType.name)
            putExtra(EXTRA_MINUTES_BEFORE, config.minutesBefore)
            putExtra(EXTRA_IS_PRE_FAJR, false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(prayerName),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
        }
    }

    /**
     * Schedule Pre-Fajr (Tahajjud / Sehri) Alarm
     */
    @SuppressLint("ScheduleExactAlarm")
    fun schedulePreFajrAlarm(context: Context, config: PreFajrAlarmConfig) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!config.isEnabled) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_PRE_FAJR,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
            return
        }

        val repository = PrayerTimeRepository()
        val cityPrefs = CityPreferences(context)
        val defaultCity = cityPrefs.getSavedCity()
        val calcMethod = cityPrefs.getSavedCalculationMethod()
        val asrMadhab = cityPrefs.getSavedAsrMadhab()
        val now = LocalDateTime.now()

        val todaySchedule = repository.getPrayerSchedule(
            date = LocalDate.now(),
            currentTime = LocalTime.now(),
            latitude = defaultCity.latitude,
            longitude = defaultCity.longitude,
            timeZoneId = defaultCity.timeZoneId,
            locationName = defaultCity.name,
            method = calcMethod,
            madhab = asrMadhab
        )

        val fajrItem = todaySchedule.prayers.firstOrNull { it.name == PrayerName.FAJR } ?: return
        var triggerDateTime = todaySchedule.date.atTime(fajrItem.time).minusMinutes(config.minutesBeforeFajr.toLong())

        if (triggerDateTime.isBefore(now)) {
            val tomorrow = LocalDate.now().plusDays(1)
            val tomorrowSchedule = repository.getPrayerSchedule(
                date = tomorrow,
                currentTime = LocalTime.MIN,
                latitude = defaultCity.latitude,
                longitude = defaultCity.longitude,
                timeZoneId = defaultCity.timeZoneId,
                locationName = defaultCity.name,
                method = calcMethod,
                madhab = asrMadhab
            )
            val tomorrowFajr = tomorrowSchedule.prayers.firstOrNull { it.name == PrayerName.FAJR } ?: return
            triggerDateTime = tomorrow.atTime(tomorrowFajr.time).minusMinutes(config.minutesBeforeFajr.toLong())
        }

        val triggerEpochMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_PRE_FAJR_ALARM
            putExtra(EXTRA_PRAYER_NAME, PrayerName.FAJR.name)
            putExtra(EXTRA_TONE_TYPE, config.toneType.name)
            putExtra(EXTRA_MINUTES_BEFORE, config.minutesBeforeFajr)
            putExtra(EXTRA_IS_PRE_FAJR, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PRE_FAJR,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
        }
    }

    /**
     * Snooze alarm for X minutes (default 10 minutes)
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleSnooze(context: Context, prayerName: PrayerName, toneType: AlarmToneType, minutes: Int = 10) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerEpochMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerName.name)
            putExtra(EXTRA_TONE_TYPE, toneType.name)
            putExtra(EXTRA_MINUTES_BEFORE, 0)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SNOOZE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(context: Context, prayerName: PrayerName) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PRAYER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(prayerName),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun rescheduleAllAlarms(context: Context) {
        val prefs = PrayerAlarmPreferences(context)
        val configs = prefs.getAllConfigs()

        configs.values.forEach { config ->
            if (config.isEnabled) {
                scheduleAlarm(context, config.prayerName, config)
            } else {
                cancelAlarm(context, config.prayerName)
            }
        }

        // Reschedule Pre-Fajr alarm
        val preFajrConfig = prefs.getPreFajrConfig()
        schedulePreFajrAlarm(context, preFajrConfig)
    }

    fun getFormattedNextAlarmTime(context: Context, prayerName: PrayerName, config: PrayerAlarmConfig): String {
        if (!config.isEnabled) return "Off"

        val repository = PrayerTimeRepository()
        val cityPrefs = CityPreferences(context)
        val defaultCity = cityPrefs.getSavedCity()
        val calcMethod = cityPrefs.getSavedCalculationMethod()
        val asrMadhab = cityPrefs.getSavedAsrMadhab()
        val now = LocalDateTime.now()

        val todaySchedule = repository.getPrayerSchedule(
            date = LocalDate.now(),
            currentTime = LocalTime.now(),
            latitude = defaultCity.latitude,
            longitude = defaultCity.longitude,
            timeZoneId = defaultCity.timeZoneId,
            locationName = defaultCity.name,
            method = calcMethod,
            madhab = asrMadhab
        )

        val prayerItem = todaySchedule.prayers.firstOrNull { it.name == prayerName } ?: return "--"
        var triggerDateTime = todaySchedule.date.atTime(prayerItem.time).minusMinutes(config.minutesBefore.toLong())

        val isTomorrow = triggerDateTime.isBefore(now)
        if (isTomorrow) {
            val tomorrow = LocalDate.now().plusDays(1)
            val tomorrowSchedule = repository.getPrayerSchedule(
                date = tomorrow,
                currentTime = LocalTime.MIN,
                latitude = defaultCity.latitude,
                longitude = defaultCity.longitude,
                timeZoneId = defaultCity.timeZoneId,
                locationName = defaultCity.name,
                method = calcMethod,
                madhab = asrMadhab
            )
            val tomorrowPrayer = tomorrowSchedule.prayers.firstOrNull { it.name == prayerName } ?: return "--"
            triggerDateTime = tomorrow.atTime(tomorrowPrayer.time).minusMinutes(config.minutesBefore.toLong())
        }

        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        val formattedTime = triggerDateTime.format(formatter)
        return if (isTomorrow) "Tomorrow at $formattedTime" else "Today at $formattedTime"
    }

    fun getFormattedNextPreFajrTime(context: Context, config: PreFajrAlarmConfig): String {
        if (!config.isEnabled) return "Off"

        val repository = PrayerTimeRepository()
        val cityPrefs = CityPreferences(context)
        val defaultCity = cityPrefs.getSavedCity()
        val calcMethod = cityPrefs.getSavedCalculationMethod()
        val asrMadhab = cityPrefs.getSavedAsrMadhab()
        val now = LocalDateTime.now()

        val todaySchedule = repository.getPrayerSchedule(
            date = LocalDate.now(),
            currentTime = LocalTime.now(),
            latitude = defaultCity.latitude,
            longitude = defaultCity.longitude,
            timeZoneId = defaultCity.timeZoneId,
            locationName = defaultCity.name,
            method = calcMethod,
            madhab = asrMadhab
        )

        val fajrItem = todaySchedule.prayers.firstOrNull { it.name == PrayerName.FAJR } ?: return "--"
        var triggerDateTime = todaySchedule.date.atTime(fajrItem.time).minusMinutes(config.minutesBeforeFajr.toLong())

        val isTomorrow = triggerDateTime.isBefore(now)
        if (isTomorrow) {
            val tomorrow = LocalDate.now().plusDays(1)
            val tomorrowSchedule = repository.getPrayerSchedule(
                date = tomorrow,
                currentTime = LocalTime.MIN,
                latitude = defaultCity.latitude,
                longitude = defaultCity.longitude,
                timeZoneId = defaultCity.timeZoneId,
                locationName = defaultCity.name,
                method = calcMethod,
                madhab = asrMadhab
            )
            val tomorrowFajr = tomorrowSchedule.prayers.firstOrNull { it.name == PrayerName.FAJR } ?: return "--"
            triggerDateTime = tomorrow.atTime(tomorrowFajr.time).minusMinutes(config.minutesBeforeFajr.toLong())
        }

        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        val formattedTime = triggerDateTime.format(formatter)
        return if (isTomorrow) "Tomorrow at $formattedTime" else "Today at $formattedTime"
    }
}
