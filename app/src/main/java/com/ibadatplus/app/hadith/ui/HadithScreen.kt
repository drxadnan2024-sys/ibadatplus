package com.ibadatplus.app.hadith.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibadatplus.app.hadith.model.TranslationLanguage
import com.ibadatplus.app.hadith.ui.components.DailyHadithCard
import com.ibadatplus.app.hadith.ui.components.HadithCard
import com.ibadatplus.app.hadith.viewmodel.HadithViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HadithViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.isShowingFavoritesOnly) "Saved Favorites" else "Hadith Sharif",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F5132)
                        )
                        Text(
                            text = "Sahih al-Bukhari & Sahih Muslim",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F5132)
                        )
                    }
                },
                actions = {
                    // Language Switcher (Urdu / English)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val newLang = if (uiState.selectedLanguage == TranslationLanguage.URDU)
                                    TranslationLanguage.ENGLISH else TranslationLanguage.URDU
                                viewModel.onLanguageChanged(newLang)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color(0xFF198754),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.selectedLanguage.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF198754)
                            )
                        }
                    }

                    // Favorites Filter Toggle
                    IconButton(onClick = { viewModel.toggleFavoritesOnlyView() }) {
                        Icon(
                            imageVector = if (uiState.isShowingFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites",
                            tint = if (uiState.isShowingFavoritesOnly) Color(0xFFEF4444) else Color(0xFF198754)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF198754))
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(2.dp)) }

                // 1. Search Bar
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Search hadith, words, number...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF6B7280)
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF6B7280)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                // 2. Book Selector Chips (All, Sahih Bukhari, Sahih Muslim)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val books = listOf(
                            "all" to "All Books (تمام کتب)",
                            "bukhari" to "Sahih Bukhari (صحیح بخاری)",
                            "muslim" to "Sahih Muslim (صحیح مسلم)"
                        )

                        books.forEach { (bookId, label) ->
                            val isSelected = uiState.selectedBookId == bookId
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF198754) else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF198754) else Color(0xFFE5E7EB)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.onBookSelected(bookId) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF374151),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Chapter Filter Chips (Horizontal scroll)
                if (uiState.availableChapters.size > 1) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // "All Chapters" chip
                            val isAllSelected = uiState.selectedChapter == null
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAllSelected) Color(0xFFE8F5E9) else Color(0xFFF3F4F6),
                                border = BorderStroke(
                                    1.dp,
                                    if (isAllSelected) Color(0xFF198754) else Color.Transparent
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.onChapterSelected(null) }
                            ) {
                                Text(
                                    text = "All Chapters",
                                    fontSize = 11.sp,
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAllSelected) Color(0xFF0F5132) else Color(0xFF6B7280),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }

                            // Individual Chapters
                            uiState.availableChapters.forEach { chapter ->
                                val isSelected = uiState.selectedChapter == chapter
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF3F4F6),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF198754) else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.onChapterSelected(chapter) }
                                ) {
                                    Text(
                                        text = chapter,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF0F5132) else Color(0xFF6B7280),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. "Aaj Ki Hadees" (Daily Hadith Card) - shown when not searching & not in favorites view
                if (uiState.dailyHadith != null && uiState.searchQuery.isBlank() && !uiState.isShowingFavoritesOnly) {
                    item {
                        DailyHadithCard(
                            hadith = uiState.dailyHadith!!,
                            language = uiState.selectedLanguage,
                            onFavoriteToggle = { viewModel.toggleFavorite(uiState.dailyHadith!!) },
                            onShare = { viewModel.shareHadith(context, uiState.dailyHadith!!) }
                        )
                    }
                }

                // 5. Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isShowingFavoritesOnly) "SAVED FAVORITES (${uiState.hadiths.size})" else "HADITH LIST (${uiState.hadiths.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )

                        if (uiState.isShowingFavoritesOnly) {
                            Text(
                                text = "Show All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF198754),
                                modifier = Modifier
                                    .clickable { viewModel.toggleFavoritesOnlyView() }
                                    .padding(end = 4.dp)
                            )
                        }
                    }
                }

                // 6. Hadiths List
                if (uiState.hadiths.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (uiState.isShowingFavoritesOnly) "No Favorite Hadiths Yet" else "No Hadiths Found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF374151)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (uiState.isShowingFavoritesOnly) "Tap the heart icon on any Hadith to save it here for quick access." else "Try searching for a different keyword or clearing filters.",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    items(uiState.hadiths, key = { it.id }) { hadith ->
                        HadithCard(
                            hadith = hadith,
                            language = uiState.selectedLanguage,
                            onFavoriteToggle = { viewModel.toggleFavorite(hadith) },
                            onShare = { viewModel.shareHadith(context, hadith) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(28.dp)) }
            }
        }
    }
}
