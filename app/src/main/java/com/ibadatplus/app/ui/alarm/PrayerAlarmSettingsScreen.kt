package com.ibadatplus.app.ui.alarm

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibadatplus.app.alarm.PrayerAlarmPreferences
import com.ibadatplus.app.alarm.PrayerAlarmScheduler
import com.ibadatplus.app.data.model.PrayerAlarmConfig
import com.ibadatplus.app.ui.alarm.components.BatteryOptimizationGuideCard
import com.ibadatplus.app.ui.alarm.components.MasjidAutoSilentCard
import com.ibadatplus.app.ui.alarm.components.PreFajrAlarmCard
import com.ibadatplus.app.ui.alarm.components.PrayerAlarmCard
import com.ibadatplus.app.viewmodel.PrayerAlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerAlarmSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrayerAlarmViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.refreshPermissions(context)
    }

    LaunchedEffect(Unit) {
        try {
            viewModel.loadSettings(context)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Namaz Alarm & Azan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F5132)
                        )
                        Text(
                            text = "Set exact alarms & reminders for all 5 prayers",
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
                CircularProgressIndicator(color = Color(0xFF10B981))
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

                // 1. Android 12+ Exact Alarm Permission Warning
                if (!uiState.isExactAlarmPermissionGranted) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Exact Alarm Permission Required",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Android 12+ requires permission to ring alarms at the exact namaz time. Please allow exact alarms in settings.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7F1D1D),
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        try {
                                            context.startActivity(PrayerAlarmScheduler.createExactAlarmSettingIntent(context))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Grant Exact Alarm Permission", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // 2. Android 13+ Notification Permission Warning
                if (!uiState.isNotificationPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            border = BorderStroke(1.dp, Color(0xFF93C5FD))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Notification Permission Disabled",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E40AF)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Please enable notifications so azan and prayer reminders can alert you.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Enable Notifications", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // 3. Auto-Silent (Masjid Mode) Card
                item {
                    MasjidAutoSilentCard(
                        config = uiState.autoSilentConfig,
                        isDndGranted = uiState.isDndPolicyAccessGranted,
                        onToggle = { enabled ->
                            viewModel.toggleAutoSilent(context, enabled)
                        },
                        onStartMinutesChanged = { startMinutes ->
                            viewModel.updateAutoSilentStartMinutes(context, startMinutes)
                        },
                        onDurationChanged = { minutes ->
                            viewModel.updateAutoSilentDuration(context, minutes)
                        },
                        onUseJamatChanged = { useJamat ->
                            viewModel.updateAutoSilentUseJamat(context, useJamat)
                        },
                        onModeChanged = { mode ->
                            viewModel.updateAutoSilentMode(context, mode)
                        },
                        onTogglePrayer = { prayerName ->
                            viewModel.toggleAutoSilentPrayer(context, prayerName)
                        }
                    )
                }

                // 4. Pre-Fajr Alarm (Tahajjud & Sehri) Card
                item {
                    PreFajrAlarmCard(
                        config = uiState.preFajrConfig,
                        nextTimeFormatted = uiState.nextPreFajrTime,
                        onToggle = { enabled ->
                            viewModel.togglePreFajrAlarm(context, enabled)
                        },
                        onOffsetChanged = { minutes ->
                            viewModel.updatePreFajrOffset(context, minutes)
                        }
                    )
                }

                // 5. OEM Battery Optimization Guide Card
                item {
                    BatteryOptimizationGuideCard(
                        isIgnoringOptimization = uiState.isIgnoringBatteryOptimizations,
                        onOptimizeClicked = {
                            viewModel.refreshPermissions(context)
                        }
                    )
                }

                // 6. Section Title
                item {
                    Text(
                        text = "5 DAILY PRAYER ALARMS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                    )
                }

                // 7. Individual Alarm Cards for each of the 5 prayers
                items(PrayerAlarmPreferences.SUPPORTED_PRAYERS) { prayerName ->
                    val config = uiState.configs[prayerName] ?: PrayerAlarmConfig(prayerName = prayerName)
                    val nextTime = uiState.nextScheduledTimes[prayerName] ?: "--"

                    PrayerAlarmCard(
                        config = config,
                        nextScheduledTime = nextTime,
                        onToggle = { enabled ->
                            viewModel.toggleAlarm(context, prayerName, enabled)
                        },
                        onToneChanged = { tone ->
                            viewModel.updateToneType(context, prayerName, tone)
                        },
                        onMinutesBeforeChanged = { minutes ->
                            viewModel.updateMinutesBefore(context, prayerName, minutes)
                        },
                        onTestAlarm = {
                            viewModel.triggerTestAlarm(context, prayerName)
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(28.dp)) }
            }
        }
    }
}
