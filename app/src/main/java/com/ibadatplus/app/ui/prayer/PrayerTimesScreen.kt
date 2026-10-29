package com.ibadatplus.app.ui.prayer

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibadatplus.app.alarm.PrayerAlarmPreferences
import com.ibadatplus.app.alarm.PrayerAlarmScheduler
import com.ibadatplus.app.auth.AuthManager
import com.ibadatplus.app.auth.AuthUser
import com.ibadatplus.app.data.model.BellAlarmState
import com.ibadatplus.app.data.model.PrayerItem
import com.ibadatplus.app.data.model.PrayerName
import com.ibadatplus.app.data.model.PrayerTimesUiState
import com.ibadatplus.app.hadith.repository.HadithRepository
import com.ibadatplus.app.ui.prayer.components.CitySelectionDialog
import com.ibadatplus.app.ui.prayer.components.JamatTimePickerDialog
import com.ibadatplus.app.ui.prayer.components.NextPrayerCard
import com.ibadatplus.app.ui.prayer.components.PrayerSettingsDialog
import com.ibadatplus.app.ui.prayer.components.PrayerTimeRow
import com.ibadatplus.app.viewmodel.PrayerTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    modifier: Modifier = Modifier,
    onNavigateToAlarmSettings: () -> Unit = {},
    onNavigateToQiblaFinder: () -> Unit = {},
    onNavigateToHadith: () -> Unit = {},
    onNavigateToTasbeeh: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    viewModel: PrayerTimeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val dailyHadith = remember { HadithRepository(context).getDailyHadith() }
    val alarmPrefs = remember { PrayerAlarmPreferences(context) }
    var alarmConfigs by remember { mutableStateOf(alarmPrefs.getAllConfigs()) }

    var showCityDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf(AuthManager.getCurrentUser()) }
    var selectedPrayerForJamatEdit by remember { mutableStateOf<PrayerItem?>(null) }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.fetchCurrentLocationAndTimes(context)
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    // Auto-request location permission on first composition ONLY if user has not picked a manual city
    LaunchedEffect(Unit) {
        if (!viewModel.isManualCitySaved()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ibadat Plus",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F5132)
                        )
                        Text(
                            text = "Prayer Times & Adhan Schedule",
                            fontSize = 12.sp,
                            color = Color(0xFF6C757D)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "Sync GPS",
                            tint = Color(0xFF198754)
                        )
                    }
                    IconButton(onClick = onNavigateToTasbeeh) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Digital Tasbeeh",
                            tint = Color(0xFF198754)
                        )
                    }
                    IconButton(onClick = onNavigateToHadith) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Hadith Sharif",
                            tint = Color(0xFF198754)
                        )
                    }
                    IconButton(onClick = onNavigateToQiblaFinder) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Qibla Finder",
                            tint = Color(0xFF198754)
                        )
                    }
                    IconButton(onClick = onNavigateToAlarmSettings) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Namaz Alarms",
                            tint = Color(0xFF198754)
                        )
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PrayerTimesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF198754))
                    }
                }

                is PrayerTimesUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Could not load prayer times",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC3545)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            fontSize = 14.sp,
                            color = Color(0xFF6C757D)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCityDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))
                        ) {
                            Text("Select City Manually")
                        }
                    }
                }

                is PrayerTimesUiState.Success -> {
                    val schedule = state.schedule

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(2.dp)) }

                        // 1. English Date & Hijri Date Banner with User Account Pill
                        item {
                            DateHeaderCard(
                                englishDate = schedule.englishDateFormatted,
                                hijriDate = schedule.hijriDateFormatted,
                                currentUser = currentUser,
                                onAccountClick = {
                                    currentUser = AuthManager.getCurrentUser()
                                    showAccountDialog = true
                                }
                            )
                        }

                        // 2. Location Indicator & Manual City Selection Chip
                        item {
                            LocationSelectionBar(
                                locationName = schedule.locationName,
                                isGps = state.isGpsLocation,
                                onChangeCityClicked = { showCityDialog = true }
                            )
                        }

                        // 3. Prominent "Next Namaz" Card with 12-Hour Time & Live Countdown
                        item {
                            NextPrayerCard(
                                nextPrayer = schedule.nextPrayer,
                                countdownText = state.formattedCountdown,
                                progress = state.progressToNextPrayer
                            )
                        }

                        // 4. Method & Madhab Summary Chip (Tap to configure)
                        item {
                            MethodSummaryChip(
                                methodName = state.currentMethod.title,
                                madhabName = state.currentMadhab.displayName,
                                onClick = { showSettingsDialog = true }
                            )
                        }

                        // 5. Section Title: Today's Prayers
                        item {
                            Text(
                                text = "TODAY'S SCHEDULE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C757D),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        // 6. List of 6 Prayers (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha) in 12-Hour format with 3-State Bell & Jamat Time
                        items(schedule.prayers) { prayerItem ->
                            val bellState = alarmConfigs[prayerItem.name]?.bellState ?: BellAlarmState.FULL_AZAN
                            PrayerTimeRow(
                                prayerItem = prayerItem,
                                bellState = bellState,
                                onBellClick = if (prayerItem.name != PrayerName.SUNRISE) {
                                    {
                                        alarmPrefs.cycleBellState(prayerItem.name)
                                        val updatedConfig = alarmPrefs.getAlarmConfig(prayerItem.name)
                                        if (updatedConfig.isEnabled) {
                                            PrayerAlarmScheduler.scheduleAlarm(context, prayerItem.name, updatedConfig)
                                        } else {
                                            PrayerAlarmScheduler.cancelAlarm(context, prayerItem.name)
                                        }
                                        alarmConfigs = alarmPrefs.getAllConfigs()
                                    }
                                } else null,
                                onEditJamatClick = if (prayerItem.name != PrayerName.SUNRISE) {
                                    {
                                        selectedPrayerForJamatEdit = prayerItem
                                    }
                                } else null
                            )
                        }

                        // 7. "Aaj Ki Hadees" (Daily Hadith on Home)
                        if (dailyHadith != null) {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable { onNavigateToHadith() },
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFD1FAE5)),
                                    shadowElevation = 2.dp
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = Color(0xFFE8F5E9),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "✨ AAJ KI HADEES",
                                                    color = Color(0xFF0F5132),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            Text(
                                                text = "All Hadiths →",
                                                color = Color(0xFF198754),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = dailyHadith.arabic,
                                            fontSize = 15.sp,
                                            lineHeight = 24.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1F2937),
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "“${dailyHadith.translationUrdu}”",
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp,
                                            color = Color(0xFF4B5563),
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "— ${dailyHadith.reference} (${dailyHadith.narrator})",
                                            fontSize = 11.sp,
                                            color = Color(0xFF198754),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }

                    // Dialogs
                    if (showCityDialog) {
                        CitySelectionDialog(
                            availableCities = state.availableCities,
                            selectedCity = state.currentCity,
                            onCitySelected = { city ->
                                viewModel.selectCity(city)
                            },
                            onDismissRequest = { showCityDialog = false }
                        )
                    }

                    if (showSettingsDialog) {
                        PrayerSettingsDialog(
                            currentMethod = state.currentMethod,
                            currentMadhab = state.currentMadhab,
                            onMethodSelected = { method ->
                                viewModel.updateCalculationMethod(method)
                            },
                            onMadhabSelected = { madhab ->
                                viewModel.updateAsrMadhab(madhab)
                            },
                            onDismissRequest = { showSettingsDialog = false }
                        )
                    }

                    // 3. Local Masjid Jamat Adjustment TimePicker Dialog
                    selectedPrayerForJamatEdit?.let { prayer ->
                        JamatTimePickerDialog(
                            prayerItem = prayer,
                            defaultOffsetMinutes = viewModel.getDefaultJamatOffset(prayer.name),
                            onSaveJamatTime = { pickedTime ->
                                viewModel.saveCustomJamatTime(prayer.name, pickedTime)
                                val autoSilent = alarmPrefs.getAutoSilentConfig()
                                if (autoSilent.isEnabled && autoSilent.useJamatTime) {
                                    PrayerAlarmScheduler.rescheduleAutoSilentAlarms(context)
                                }
                            },
                            onResetToDefault = {
                                viewModel.resetJamatTimeToDefault(prayer.name)
                                val autoSilent = alarmPrefs.getAutoSilentConfig()
                                if (autoSilent.isEnabled && autoSilent.useJamatTime) {
                                    PrayerAlarmScheduler.rescheduleAutoSilentAlarms(context)
                                }
                            },
                            onDismiss = { selectedPrayerForJamatEdit = null }
                        )
                    }
                }
            }

            if (showAccountDialog) {
                UserAccountDialog(
                    currentUser = currentUser,
                    onDismiss = { showAccountDialog = false },
                    onLoginClick = {
                        showAccountDialog = false
                        onNavigateToAuth()
                    },
                    onLogoutClick = {
                        AuthManager.signOut(context)
                        currentUser = null
                        viewModel.reloadForUser()
                        alarmConfigs = alarmPrefs.getAllConfigs()
                        showAccountDialog = false
                        onNavigateToAuth()
                    }
                )
            }
        }
    }
}

/**
 * Clean Date Banner showing both English and Hijri dates with User Account Badge
 */
@Composable
private fun DateHeaderCard(
    englishDate: String,
    hijriDate: String,
    currentUser: AuthUser?,
    onAccountClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date",
                            tint = Color(0xFF198754),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = englishDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212529)
                    )
                    Text(
                        text = hijriDate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF198754)
                    )
                }
            }

            // User Profile / Account Badge
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onAccountClick() },
                color = if (currentUser != null && !currentUser.isAnonymous) Color(0xFFE8F5E9) else Color(0xFFF1F3F5),
                border = BorderStroke(
                    1.dp,
                    if (currentUser != null && !currentUser.isAnonymous) Color(0xFF86EFAC) else Color(0xFFE2E8F0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account",
                        tint = if (currentUser != null && !currentUser.isAnonymous) Color(0xFF16A34A) else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentUser != null && !currentUser.isAnonymous) {
                            currentUser.phoneNumber ?: currentUser.email?.substringBefore("@") ?: "User"
                        } else {
                            "Guest"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (currentUser != null && !currentUser.isAnonymous) Color(0xFF15803D) else Color(0xFF475569)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserAccountDialog(
    currentUser: AuthUser?,
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color(0xFF198754),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentUser != null && !currentUser.isAnonymous) "User Account" else "Guest Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (currentUser != null && !currentUser.isAnonymous) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "LOGGED IN AS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentUser.phoneNumber ?: currentUser.email ?: "Authenticated User",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "UID: ${currentUser.uid}",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Text(
                        text = "Your Tasbeeh counts, custom dhikrs, mosque Jamat offsets, and prayer alarm preferences are securely isolated and linked only to your UID.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 17.sp
                    )
                } else {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "CURRENT STATUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Guest User (अतिथी मोड)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                        }
                    }

                    Text(
                        text = "Log in with your phone number via real SMS OTP to keep your Tasbeeh counts and personalized settings separated across multiple users on this device.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 17.sp
                    )
                }
            }
        },
        confirmButton = {
            if (currentUser != null && !currentUser.isAnonymous) {
                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Log Out / Switch Account", color = Color.White)
                }
            } else {
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))
                ) {
                    Text("Sign In with Mobile OTP", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF198754)
                )
            }
        }
    }
}

/**
 * Bar showing active location (GPS vs City) and manual selection button
 */
@Composable
private fun LocationSelectionBar(
    locationName: String,
    isGps: Boolean,
    onChangeCityClicked: () -> Unit
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isGps) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (isGps) Color(0xFF198754) else Color(0xFF0D6EFD),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = locationName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212529),
                        maxLines = 1
                    )
                    Text(
                        text = if (isGps) "Auto-detected via GPS" else "Manual city selection",
                        fontSize = 11.sp,
                        color = Color(0xFF868E96)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onChangeCityClicked() },
                color = Color(0xFFF1F3F5)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = null,
                        tint = Color(0xFF495057),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Change City",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF495057)
                    )
                }
            }
        }
    }
}

/**
 * Chip indicating the active calculation method and Asr madhab
 */
@Composable
private fun MethodSummaryChip(
    methodName: String,
    madhabName: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = Color(0xFFE9ECEF)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = Color(0xFF495057),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Method: $methodName • Asr: $madhabName",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF495057)
                )
            }

            Text(
                text = "Edit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF198754)
            )
        }
    }
}
