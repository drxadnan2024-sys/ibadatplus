package com.ibadatplus.app.ui.alarm.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadatplus.app.alarm.autosilent.AutoSilentManager
import com.ibadatplus.app.data.model.AutoSilentConfig
import com.ibadatplus.app.data.model.AutoSilentMode
import com.ibadatplus.app.data.model.PrayerName

@Composable
fun MasjidAutoSilentCard(
    config: AutoSilentConfig,
    isDndGranted: Boolean,
    onToggle: (Boolean) -> Unit,
    onStartMinutesChanged: (Int) -> Unit,
    onDurationChanged: (Int) -> Unit,
    onUseJamatChanged: (Boolean) -> Unit,
    onModeChanged: (AutoSilentMode) -> Unit,
    onTogglePrayer: (PrayerName) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEnabled = config.isEnabled

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) Color(0xFFF0FDF4) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isEnabled) Color(0xFF86EFAC) else Color(0xFFE5E7EB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEnabled) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isEnabled) Color(0xFF10B981) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Auto-Silent (Masjid Mode)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) Color(0xFF0F5132) else Color(0xFF1F2937)
                        )
                        Text(
                            text = if (isEnabled) "Active: ${config.displayWindowSummary}" else "Namaz me phone automatically silent karein",
                            fontSize = 11.sp,
                            color = if (isEnabled) Color(0xFF16A34A) else Color(0xFF6B7280)
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981)
                    )
                )
            }

            AnimatedVisibility(visible = isEnabled) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Divider(color = Color(0x33000000))
                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Silent Window Summary Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Window: ${config.displayWindowSummary}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF166534)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Reference Time: Align with Jamat vs Azaan
                    Text(
                        text = "ALIGN SILENT WINDOW WITH:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Jamat Time Chip
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (config.useJamatTime) Color(0xFF10B981) else Color(0xFFF3F4F6),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onUseJamatChanged(true) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = if (config.useJamatTime) Color.White else Color(0xFF4B5563),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Masjid Jamat",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (config.useJamatTime) Color.White else Color(0xFF374151)
                                )
                            }
                        }

                        // Azaan Time Chip
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!config.useJamatTime) Color(0xFF10B981) else Color(0xFFF3F4F6),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onUseJamatChanged(false) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = null,
                                    tint = if (!config.useJamatTime) Color.White else Color(0xFF4B5563),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Azaan Time",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!config.useJamatTime) Color.White else Color(0xFF374151)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Start Window (When to silence)
                    Text(
                        text = "START SILENT WINDOW:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val startOffsets = listOf(0 to "At Time", 5 to "5m before", 10 to "10m before", 15 to "15m before")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        startOffsets.forEach { (minutes, label) ->
                            val isSelected = minutes == config.startMinutesBefore
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF10B981) else Color(0xFFF3F4F6),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onStartMinutesChanged(minutes) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF374151),
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Total Duration (Until when to silence)
                    Text(
                        text = "TOTAL SILENT DURATION (RESTORES AUTOMATICALLY):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val durations = listOf(15, 20, 25, 30, 45)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durations.forEach { minutes ->
                            val isSelected = minutes == config.durationMinutes
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF10B981) else Color(0xFFF3F4F6),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onDurationChanged(minutes) }
                            ) {
                                Text(
                                    text = "$minutes m",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF374151),
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Per-Prayer Active Chips
                    Text(
                        text = "APPLY TO PRAYERS:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            PrayerName.FAJR,
                            PrayerName.DHUHR,
                            PrayerName.ASR,
                            PrayerName.MAGHRIB,
                            PrayerName.ISHA
                        ).forEach { prayer ->
                            val isPrayerActive = config.enabledPrayers.contains(prayer)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPrayerActive) Color(0xFFE8F5E9) else Color(0xFFF3F4F6),
                                border = BorderStroke(
                                    1.dp,
                                    if (isPrayerActive) Color(0xFF10B981) else Color(0xFFE5E7EB)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onTogglePrayer(prayer) }
                            ) {
                                Text(
                                    text = prayer.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isPrayerActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPrayerActive) Color(0xFF0F5132) else Color(0xFF9CA3AF),
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. Mode Selector: Vibrate vs DND Silent
                    Text(
                        text = "SILENT MODE TYPE:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Vibrate mode chip
                        val isVibrate = config.silentMode == AutoSilentMode.VIBRATE
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isVibrate) Color(0xFFECFDF5) else Color(0xFFF3F4F6),
                            border = BorderStroke(
                                1.dp,
                                if (isVibrate) Color(0xFF10B981) else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onModeChanged(AutoSilentMode.VIBRATE) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = if (isVibrate) Color(0xFF047857) else Color(0xFF6B7280),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Vibrate Mode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isVibrate) Color(0xFF047857) else Color(0xFF374151)
                                    )
                                    Text(
                                        text = "Works everywhere",
                                        fontSize = 10.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        }

                        // DND Silent chip
                        val isDnd = config.silentMode == AutoSilentMode.DND_SILENT
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDnd) Color(0xFFECFDF5) else Color(0xFFF3F4F6),
                            border = BorderStroke(
                                1.dp,
                                if (isDnd) Color(0xFF10B981) else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onModeChanged(AutoSilentMode.DND_SILENT) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoNotDisturbOn,
                                    contentDescription = null,
                                    tint = if (isDnd) Color(0xFF047857) else Color(0xFF6B7280),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "DND / Pure Silent",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isDnd) Color(0xFF047857) else Color(0xFF374151)
                                    )
                                    Text(
                                        text = if (isDndGranted) "Access granted" else "Needs DND permission",
                                        fontSize = 10.sp,
                                        color = if (isDndGranted) Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }

                    // DND Permission prompt if DND mode selected without permission
                    if (config.silentMode == AutoSilentMode.DND_SILENT && !isDndGranted) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(AutoSilentManager.createDndSettingsIntent())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5132)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant Do Not Disturb (DND) Access", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
