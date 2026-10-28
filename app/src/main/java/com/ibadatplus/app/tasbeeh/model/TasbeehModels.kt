package com.ibadatplus.app.tasbeeh.model

/**
 * Predefined or custom Dhikr item
 */
data class DhikrItem(
    val id: String,
    val nameEnglish: String,
    val nameArabic: String,
    val defaultTarget: Int, // 0 means Unlimited / Free mode
    val meaning: String,
    val isCustom: Boolean = false
)

/**
 * Default preset Dhikrs according to authentic Sunnah
 */
object DefaultDhikrs {
    val PRESETS = listOf(
        DhikrItem(
            id = "subhanallah",
            nameEnglish = "SubhanAllah",
            nameArabic = "سُبْحَانَ اللَّهِ",
            defaultTarget = 33,
            meaning = "Glory be to Allah (پاک ہے اللہ)"
        ),
        DhikrItem(
            id = "alhamdulillah",
            nameEnglish = "Alhamdulillah",
            nameArabic = "الْحَمْدُ لِلَّهِ",
            defaultTarget = 33,
            meaning = "All praise is for Allah (سب تعریف اللہ کے لیے ہے)"
        ),
        DhikrItem(
            id = "allahuakbar",
            nameEnglish = "Allahu Akbar",
            nameArabic = "اللَّهُ أَكْبَرُ",
            defaultTarget = 34,
            meaning = "Allah is the Greatest (اللہ سب سے بڑا ہے)"
        ),
        DhikrItem(
            id = "astaghfirullah",
            nameEnglish = "Astaghfirullah",
            nameArabic = "أَسْتَغْفِرُ اللَّهَ",
            defaultTarget = 100,
            meaning = "I seek forgiveness from Allah (میں اللہ سے بخشش مانگتا ہوں)"
        ),
        DhikrItem(
            id = "durood",
            nameEnglish = "Durood Shareef",
            nameArabic = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ",
            defaultTarget = 100,
            meaning = "Blessings upon the Prophet ﷺ (درود شریف)"
        ),
        DhikrItem(
            id = "lailahaillallah",
            nameEnglish = "La ilaha illallah",
            nameArabic = "لَا إِلٰهَ إِلَّا اللّٰهُ",
            defaultTarget = 100,
            meaning = "None is worthy of worship but Allah (اللہ کے سوا کوئی معبود نہیں)"
        ),
        DhikrItem(
            id = "ayatulkursi",
            nameEnglish = "Ayat al-Kursi",
            nameArabic = "آيَةُ الْكُرْسِيِّ",
            defaultTarget = 7,
            meaning = "The Throne Verse"
        )
    )
}

/**
 * UI State for the Digital Tasbeeh Counter
 */
data class TasbeehState(
    val currentDhikr: DhikrItem = DefaultDhikrs.PRESETS.first(),
    val currentCount: Int = 0,
    val targetCount: Int = 33, // 0 = Free / Unlimited Mode
    val lifetimeTotalCount: Long = 0L,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isTargetReached: Boolean = false,
    val allDhikrs: List<DhikrItem> = DefaultDhikrs.PRESETS,
    val isCustomDhikrDialogOpen: Boolean = false,
    val isResetDialogOpen: Boolean = false,
    val isSettingsDialogOpen: Boolean = false
) {
    val isFreeMode: Boolean
        get() = targetCount <= 0

    val progress: Float
        get() = if (isFreeMode || targetCount == 0) {
            (currentCount % 100) / 100f
        } else {
            (currentCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)
        }

    val targetDisplayLabel: String
        get() = if (isFreeMode) "∞ (Free)" else "$targetCount"
}
