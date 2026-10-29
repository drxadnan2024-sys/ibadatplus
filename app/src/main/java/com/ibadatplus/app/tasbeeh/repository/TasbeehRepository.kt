package com.ibadatplus.app.tasbeeh.repository

import android.content.Context
import android.content.SharedPreferences
import com.ibadatplus.app.auth.AuthManager
import com.ibadatplus.app.tasbeeh.model.DefaultDhikrs
import com.ibadatplus.app.tasbeeh.model.DhikrItem
import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles persistent offline storage of Tasbeeh counts, lifetime totals,
 * target preferences, sound/vibration toggles, and custom Dhikrs.
 * Fully isolated by authenticated User UID for multi-user support.
 */
class TasbeehRepository(
    context: Context,
    userId: String = AuthManager.getCurrentUserId()
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("${PREFS_NAME}_$userId", Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ibadat_tasbeeh_prefs"
        private const val KEY_ACTIVE_DHIKR_ID = "active_dhikr_id"
        private const val KEY_LIFETIME_COUNT = "lifetime_total_dhikr_count"
        private const val KEY_COUNT_PREFIX = "count_dhikr_"
        private const val KEY_TARGET_PREFIX = "target_dhikr_"
        private const val KEY_SOUND_ENABLED = "tasbeeh_sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "tasbeeh_vibration_enabled"
        private const val KEY_CUSTOM_DHIKRS_JSON = "custom_dhikrs_json"
    }

    fun getActiveDhikrId(): String {
        return prefs.getString(KEY_ACTIVE_DHIKR_ID, DefaultDhikrs.PRESETS.first().id)
            ?: DefaultDhikrs.PRESETS.first().id
    }

    fun saveActiveDhikrId(id: String) {
        prefs.edit().putString(KEY_ACTIVE_DHIKR_ID, id).apply()
    }

    fun getCurrentCount(dhikrId: String): Int {
        return prefs.getInt(KEY_COUNT_PREFIX + dhikrId, 0)
    }

    fun saveCurrentCount(dhikrId: String, count: Int) {
        prefs.edit().putInt(KEY_COUNT_PREFIX + dhikrId, count).apply()
    }

    fun getTargetCount(dhikrId: String, defaultTarget: Int): Int {
        return prefs.getInt(KEY_TARGET_PREFIX + dhikrId, defaultTarget)
    }

    fun saveTargetCount(dhikrId: String, target: Int) {
        prefs.edit().putInt(KEY_TARGET_PREFIX + dhikrId, target).apply()
    }

    fun getLifetimeTotalCount(): Long {
        return prefs.getLong(KEY_LIFETIME_COUNT, 0L)
    }

    fun incrementLifetimeCount(): Long {
        val current = getLifetimeTotalCount() + 1
        prefs.edit().putLong(KEY_LIFETIME_COUNT, current).apply()
        return current
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isVibrationEnabled(): Boolean {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    fun getCustomDhikrs(): List<DhikrItem> {
        val json = prefs.getString(KEY_CUSTOM_DHIKRS_JSON, null) ?: return emptyList()
        val list = mutableListOf<DhikrItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DhikrItem(
                        id = obj.getString("id"),
                        nameEnglish = obj.getString("nameEnglish"),
                        nameArabic = obj.getString("nameArabic"),
                        defaultTarget = obj.getInt("defaultTarget"),
                        meaning = obj.getString("meaning"),
                        isCustom = true
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveCustomDhikr(dhikr: DhikrItem) {
        val list = getCustomDhikrs().toMutableList()
        // If already exists, replace; else append
        val index = list.indexOfFirst { it.id == dhikr.id }
        if (index != -1) {
            list[index] = dhikr
        } else {
            list.add(dhikr)
        }

        try {
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("nameEnglish", item.nameEnglish)
                    put("nameArabic", item.nameArabic)
                    put("defaultTarget", item.defaultTarget)
                    put("meaning", item.meaning)
                    put("isCustom", true)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_CUSTOM_DHIKRS_JSON, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
