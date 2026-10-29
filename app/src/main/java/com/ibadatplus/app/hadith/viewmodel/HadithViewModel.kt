package com.ibadatplus.app.hadith.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ibadatplus.app.hadith.model.HadithItem
import com.ibadatplus.app.hadith.model.HadithUiState
import com.ibadatplus.app.hadith.model.TranslationLanguage
import com.ibadatplus.app.hadith.repository.HadithRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HadithViewModel(application: Application) : AndroidViewModel(application) {

    private var repository = HadithRepository(application)

    private val _uiState = MutableStateFlow(HadithUiState())
    val uiState: StateFlow<HadithUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun refreshForUser(application: Application) {
        repository = HadithRepository(application)
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val allHadiths = repository.getAllHadiths()
            val dailyHadith = repository.getDailyHadith()
            val availableChapters = repository.getChapters("all")
            val favorites = allHadiths.filter { it.isFavorite }

            _uiState.update {
                it.copy(
                    dailyHadith = dailyHadith,
                    hadiths = allHadiths,
                    favoriteHadiths = favorites,
                    availableChapters = availableChapters,
                    isLoading = false
                )
            }
            applyFilters()
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onBookSelected(bookId: String) {
        val chapters = repository.getChapters(bookId)
        _uiState.update {
            it.copy(
                selectedBookId = bookId,
                selectedChapter = null,
                availableChapters = chapters
            )
        }
        applyFilters()
    }

    fun onChapterSelected(chapter: String?) {
        _uiState.update { it.copy(selectedChapter = chapter) }
        applyFilters()
    }

    fun onLanguageChanged(language: TranslationLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun toggleFavoritesOnlyView() {
        _uiState.update { it.copy(isShowingFavoritesOnly = !it.isShowingFavoritesOnly) }
        applyFilters()
    }

    fun toggleFavorite(hadith: HadithItem) {
        viewModelScope.launch {
            repository.toggleFavorite(hadith.id)
            val allHadiths = repository.getAllHadiths()
            val favorites = allHadiths.filter { it.isFavorite }
            val dailyHadith = repository.getDailyHadith()

            _uiState.update {
                it.copy(
                    dailyHadith = dailyHadith,
                    hadiths = allHadiths,
                    favoriteHadiths = favorites
                )
            }
            applyFilters()
        }
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val sourceList = if (currentState.isShowingFavoritesOnly) {
            currentState.favoriteHadiths
        } else {
            repository.getAllHadiths()
        }

        val isSearching = currentState.searchQuery.isNotBlank()

        val filtered = sourceList.filter { hadith ->
            val matchesBook = currentState.selectedBookId == "all" || hadith.bookId == currentState.selectedBookId
            // When actively searching, search across the book/all books so specific chapter filter doesn't hide search results
            val matchesChapter = if (isSearching) true else (currentState.selectedChapter == null || hadith.chapter == currentState.selectedChapter)
            val matchesSearch = hadith.matchesQuery(currentState.searchQuery)
            matchesBook && matchesChapter && matchesSearch
        }

        _uiState.update { it.copy(hadiths = filtered) }
    }

    fun shareHadith(context: Context, hadith: HadithItem) {
        val text = hadith.getShareableText(_uiState.value.selectedLanguage)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Hadith")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }
}
