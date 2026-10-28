package com.ibadatplus.app.hadith.model

/**
 * Single authentic Hadith entry
 */
data class HadithItem(
    val id: Int,
    val book: String,
    val bookId: String,
    val chapter: String,
    val hadithNumber: Int,
    val narrator: String,
    val arabic: String,
    val translationUrdu: String,
    val translationEnglish: String,
    val reference: String,
    val isFavorite: Boolean = false
) {
    fun matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return translationUrdu.lowercase().contains(q) ||
                translationEnglish.lowercase().contains(q) ||
                arabic.contains(q) ||
                narrator.lowercase().contains(q) ||
                chapter.lowercase().contains(q) ||
                reference.lowercase().contains(q) ||
                hadithNumber.toString().contains(q)
    }

    fun getShareableText(language: TranslationLanguage): String {
        val translation = if (language == TranslationLanguage.URDU) translationUrdu else translationEnglish
        return """
            $arabic
            
            "$translation"
            
            — $narrator
            📚 $reference ($chapter)
            Via Ibadat Plus
        """.trimIndent()
    }
}

enum class TranslationLanguage(val displayName: String) {
    URDU("اردو"),
    ENGLISH("English")
}

data class HadithUiState(
    val dailyHadith: HadithItem? = null,
    val hadiths: List<HadithItem> = emptyList(),
    val favoriteHadiths: List<HadithItem> = emptyList(),
    val availableChapters: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedBookId: String = "all", // "all", "bukhari", "muslim"
    val selectedChapter: String? = null,
    val selectedLanguage: TranslationLanguage = TranslationLanguage.URDU,
    val isShowingFavoritesOnly: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
