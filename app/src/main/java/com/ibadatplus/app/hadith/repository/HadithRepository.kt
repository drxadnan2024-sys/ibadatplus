package com.ibadatplus.app.hadith.repository

import android.content.Context
import android.content.SharedPreferences
import com.ibadatplus.app.hadith.model.HadithItem
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate

class HadithRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Cached in-memory list for instant, offline, zero-latency access
    private var cachedHadiths: List<HadithItem>? = null

    companion object {
        private const val PREFS_NAME = "ibadat_hadith_prefs"
        private const val KEY_FAVORITES = "favorite_hadith_ids"
        private const val ASSET_FILE = "hadiths.json"
    }

    /**
     * Loads Hadiths from verified offline JSON asset and attaches favorite status
     */
    fun getAllHadiths(): List<HadithItem> {
        val hadiths = cachedHadiths ?: loadHadithsFromAssets().also { cachedHadiths = it }
        val favoriteIds = getFavoriteIds()
        return hadiths.map { item ->
            item.copy(isFavorite = favoriteIds.contains(item.id))
        }
    }

    private fun loadHadithsFromAssets(): List<HadithItem> {
        val list = mutableListOf<HadithItem>()
        try {
            val inputStream = context.assets.open(ASSET_FILE)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    HadithItem(
                        id = obj.getInt("id"),
                        book = obj.getString("book"),
                        bookId = obj.getString("bookId"),
                        chapter = obj.getString("chapter"),
                        hadithNumber = obj.getInt("hadithNumber"),
                        narrator = obj.getString("narrator"),
                        arabic = obj.getString("arabic"),
                        translationUrdu = obj.getString("translationUrdu"),
                        translationEnglish = obj.getString("translationEnglish"),
                        reference = obj.getString("reference")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    /**
     * "Aaj ki Hadees": Returns today's Hadith deterministically based on day of year
     */
    fun getDailyHadith(): HadithItem? {
        val all = getAllHadiths()
        if (all.isEmpty()) return null
        val dayOfYear = LocalDate.now().dayOfYear
        val index = (dayOfYear - 1) % all.size
        return all[index]
    }

    /**
     * Returns set of saved favorite Hadith IDs
     */
    fun getFavoriteIds(): Set<Int> {
        val set = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        return set.mapNotNull { it.toIntOrNull() }.toSet()
    }

    /**
     * Toggles favorite status for a Hadith ID and returns new state
     */
    fun toggleFavorite(hadithId: Int): Boolean {
        val currentIds = getFavoriteIds().toMutableSet()
        val isNowFavorite = if (currentIds.contains(hadithId)) {
            currentIds.remove(hadithId)
            false
        } else {
            currentIds.add(hadithId)
            true
        }
        prefs.edit()
            .putStringSet(KEY_FAVORITES, currentIds.map { it.toString() }.toSet())
            .apply()

        return isNowFavorite
    }

    /**
     * Returns list of unique chapters for a given book filter
     */
    fun getChapters(bookId: String): List<String> {
        val hadiths = getAllHadiths()
        val filtered = if (bookId == "all") hadiths else hadiths.filter { it.bookId == bookId }
        return filtered.map { it.chapter }.distinct()
    }
}
