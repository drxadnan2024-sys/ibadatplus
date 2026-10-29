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
        val hadiths = cachedHadiths ?: loadHadithsFromAssets().let { loaded ->
            if (loaded.isNotEmpty()) {
                loaded.also { cachedHadiths = it }
            } else {
                getFallbackHadiths().also { cachedHadiths = it }
            }
        }
        val favoriteIds = getFavoriteIds()
        return hadiths.map { item ->
            item.copy(isFavorite = favoriteIds.contains(item.id))
        }
    }

    private fun loadHadithsFromAssets(): List<HadithItem> {
        val list = mutableListOf<HadithItem>()
        try {
            val inputStream = context.assets.open(ASSET_FILE)
            val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                .trim()
                .removePrefix("\uFEFF") // Remove UTF-8 Byte Order Mark if present

            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optInt("id", i + 1)
                val book = obj.optString("book", "Sahih al-Bukhari")
                val bookId = obj.optString("bookId", if (book.contains("Muslim", ignoreCase = true)) "muslim" else "bukhari")
                val chapter = obj.optString("chapter", "General")
                val hadithNumber = obj.optInt("hadithNumber", id)
                val narrator = obj.optString("narrator", "")
                val arabic = obj.optString("arabic", "")
                val translationUrdu = obj.optString("translationUrdu", "")
                val translationEnglish = obj.optString("translationEnglish", "")
                val reference = obj.optString("reference", "$book $hadithNumber")

                list.add(
                    HadithItem(
                        id = id,
                        book = book,
                        bookId = bookId,
                        chapter = chapter,
                        hadithNumber = hadithNumber,
                        narrator = narrator,
                        arabic = arabic,
                        translationUrdu = translationUrdu,
                        translationEnglish = translationEnglish,
                        reference = reference
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    /**
     * Authentic offline fallback dataset in case asset reading is blocked by environment
     */
    private fun getFallbackHadiths(): List<HadithItem> {
        return listOf(
            HadithItem(
                id = 1,
                book = "Sahih al-Bukhari",
                bookId = "bukhari",
                chapter = "Book of Revelation (كتاب بدء الوحى)",
                hadithNumber = 1,
                narrator = "Umar bin Al-Khattab (RA)",
                arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا، فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ.",
                translationUrdu = "اعمال کا دارومدار نیتوں پر ہے اور ہر شخص کے لیے وہی ہے جس کی اس نے نیت کی۔",
                translationEnglish = "Actions are judged by motives (intentions), so each man will have what he intended.",
                reference = "Sahih al-Bukhari 1"
            ),
            HadithItem(
                id = 5,
                book = "Sahih al-Bukhari",
                bookId = "bukhari",
                chapter = "Book of Knowledge (كتاب العلم)",
                hadithNumber = 121,
                narrator = "Abu Hurairah (RA)",
                arabic = "حَفِظْتُ مِنْ رَسُولِ اللَّهِ صلى الله عليه وسلم وِعَاءَيْنِ، فَأَمَّا أَحَدُهُمَا فَبَثَثْتُهُ، وَأَمَّا الآخَرُ فَلَوْ بَثَثْتُهُ قُطِعَ هَذَا الْبُلْعُومُ.",
                translationUrdu = "میں نے رسول اللہ ﷺ سے علم کے دو تھیلے محفوظ کیے۔ ایک تو میں نے پھیلا دیا، اور رہا دوسرا تو اگر میں اسے پھیلاؤں تو یہ حلقوم کاٹ دیا جائے۔",
                translationEnglish = "I have memorized two kinds of knowledge from Allah's Messenger (ﷺ): one of them I have propagated, but if I were to propagate the other, this pharynx (throat) would be cut.",
                reference = "Sahih al-Bukhari 121"
            ),
            HadithItem(
                id = 8,
                book = "Sahih Muslim",
                bookId = "muslim",
                chapter = "Book of Faith (كتاب الإيمان)",
                hadithNumber = 1,
                narrator = "Umar bin Al-Khattab (RA)",
                arabic = "فَأَخْبِرْنِي عَنِ الإِسْلاَمِ. قَالَ رَسُولُ اللَّهِ صلى الله عليه وسلم: الإِسْلاَمُ أَنْ تَشْهَدَ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ، وَتُقِيمَ الصَّلاَةَ، وَتُؤْتِيَ الزَّكَاةَ، وَتَصُومَ رَمَضَانَ، وَتَحُجَّ الْبَيْتَ إِنِ اسْتَطَعْتَ إِلَيْهِ سَبِيلاً.",
                translationUrdu = "رسول اللہ ﷺ نے فرمایا: اسلام یہ ہے کہ تم گواہی دو کہ اللہ کے سوا کوئی معبود نہیں اور محمد ﷺ اللہ کے رسول ہیں، نماز قائم کرو، زکوٰۃ ادا کرو، رمضان کے روزے رکھو اور اگر استطاعت ہو تو حج کرو۔",
                translationEnglish = "The Messenger of Allah (ﷺ) said: Islam is to testify that there is no god but Allah and Muhammad is the Messenger of Allah, to establish prayer, to pay Zakat, to fast in Ramadan, and to perform the pilgrimage if you have the means.",
                reference = "Sahih Muslim 1"
            ),
            HadithItem(
                id = 9,
                book = "Sahih Muslim",
                bookId = "muslim",
                chapter = "Book of Faith (كتاب الإيمان)",
                hadithNumber = 121,
                narrator = "Abu Hurairah (RA)",
                arabic = "لاَ يُلْدَغُ الْمُؤْمِنُ مِنْ جُحْرٍ وَاحِدٍ مَرَّتَيْنِ.",
                translationUrdu = "رسول اللہ ﷺ نے فرمایا: مومن ایک سوراخ (بل) سے دو مرتبہ نہیں ڈسا جاتا۔",
                translationEnglish = "The Messenger of Allah (ﷺ) said: A believer is not stung twice from the same hole.",
                reference = "Sahih Muslim 121"
            )
        )
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
