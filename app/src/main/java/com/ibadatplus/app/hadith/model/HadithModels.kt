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
        val cleanQuery = query.trim().lowercase()
        val digitsOnly = cleanQuery.filter { it.isDigit() }

        // 1. Direct Hadith number match (e.g. searching "1", "121", "#121")
        val matchesNumber = if (digitsOnly.isNotEmpty()) {
            hadithNumber.toString() == digitsOnly ||
                    hadithNumber.toString().startsWith(digitsOnly) ||
                    hadithNumber.toString().contains(digitsOnly)
        } else false

        // 2. Textual search across all relevant fields
        val matchesText = reference.lowercase().contains(cleanQuery) ||
                translationEnglish.lowercase().contains(cleanQuery) ||
                translationUrdu.lowercase().contains(cleanQuery) ||
                arabic.contains(cleanQuery) ||
                narrator.lowercase().contains(cleanQuery) ||
                chapter.lowercase().contains(cleanQuery) ||
                book.lowercase().contains(cleanQuery)

        return matchesNumber || matchesText
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
