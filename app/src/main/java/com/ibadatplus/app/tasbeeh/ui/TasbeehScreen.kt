package com.ibadatplus.app.tasbeeh.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibadatplus.app.tasbeeh.ui.components.CustomDhikrDialog
import com.ibadatplus.app.tasbeeh.ui.components.DhikrSelectionChips
import com.ibadatplus.app.tasbeeh.ui.components.ResetConfirmDialog
import com.ibadatplus.app.tasbeeh.ui.components.TargetReachedDialog
import com.ibadatplus.app.tasbeeh.ui.components.TasbeehCircularCounter
import com.ibadatplus.app.tasbeeh.ui.components.TasbeehSettingsDialog
import com.ibadatplus.app.tasbeeh.viewmodel.TasbeehViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbeehScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasbeehViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Digital Tasbeeh",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F5132)
                        )
                        Text(
                            text = "تسبیح و ذکرِ الٰہی",
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
                    // Reset Button (with confirmation)
                    IconButton(onClick = { viewModel.showResetDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Counter",
                            tint = Color(0xFFDC2626)
                        )
                    }

                    // Settings Dialog
                    IconButton(onClick = { viewModel.showSettingsDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Tasbeeh Settings",
                            tint = Color(0xFF198754)
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // 1. Lifetime Total Dhikr Tracker Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Lifetime Total Dhikr",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = String.format(Locale.ENGLISH, "%,d", state.lifetimeTotalCount),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F5132)
                                )
                            }
                        }

                        // Quick Target Indicator Chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3F4F6),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.showSettingsDialog(true) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrackChanges,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = state.targetDisplayLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }
                    }
                }

                // 2. Preset Dhikr Selection Bar (Horizontal chips)
                DhikrSelectionChips(
                    allDhikrs = state.allDhikrs,
                    selectedDhikr = state.currentDhikr,
                    onDhikrSelected = { viewModel.selectDhikr(it) },
                    onAddCustomClick = { viewModel.showCustomDhikrDialog(true) }
                )

                // 3. Active Dhikr Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.currentDhikr.nameArabic,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F5132),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = state.currentDhikr.nameEnglish,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        )

                        Text(
                            text = state.currentDhikr.meaning,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Center Tap Area (The Big Circular Counter)
                TasbeehCircularCounter(
                    currentCount = state.currentCount,
                    targetCount = state.targetCount,
                    progress = state.progress,
                    isTargetReached = state.isTargetReached,
                    onTap = { viewModel.onTapCounter() }
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 5. Bottom Quick Controls (Sound, Vibration, Target, Reset)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Sound Toggle
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound Toggle",
                            tint = if (state.isSoundEnabled) Color(0xFF10B981) else Color(0xFF9CA3AF)
                        )
                    }

                    // Quick Vibration Toggle
                    IconButton(
                        onClick = { viewModel.toggleVibration() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Vibration Toggle",
                            tint = if (state.isVibrationEnabled) Color(0xFF10B981) else Color(0xFF9CA3AF)
                        )
                    }

                    // Quick Target Switcher
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.showSettingsDialog(true) }
                    ) {
                        Text(
                            text = "Target: ${state.targetDisplayLabel}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Reset Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.showResetDialog(true) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reset",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (state.isResetDialogOpen) {
        ResetConfirmDialog(
            dhikrName = state.currentDhikr.nameEnglish,
            onConfirm = { viewModel.confirmReset() },
            onDismiss = { viewModel.showResetDialog(false) }
        )
    }

    if (state.isTargetReached) {
        TargetReachedDialog(
            dhikrName = state.currentDhikr.nameEnglish,
            targetCount = state.targetCount,
            onContinue = { viewModel.dismissTargetReached() },
            onReset = {
                viewModel.confirmReset()
            },
            onDismiss = { viewModel.dismissTargetReached() }
        )
    }

    if (state.isCustomDhikrDialogOpen) {
        CustomDhikrDialog(
            onAdd = { nameEnglish, nameArabic, target ->
                viewModel.addCustomDhikr(nameEnglish, nameArabic, target)
            },
            onDismiss = { viewModel.showCustomDhikrDialog(false) }
        )
    }

    if (state.isSettingsDialogOpen) {
        TasbeehSettingsDialog(
            isSoundEnabled = state.isSoundEnabled,
            isVibrationEnabled = state.isVibrationEnabled,
            currentTarget = state.targetCount,
            onToggleSound = { viewModel.toggleSound() },
            onToggleVibration = { viewModel.toggleVibration() },
            onSelectTarget = { viewModel.setTargetCount(it) },
            onDismiss = { viewModel.showSettingsDialog(false) }
        )
    }
}
