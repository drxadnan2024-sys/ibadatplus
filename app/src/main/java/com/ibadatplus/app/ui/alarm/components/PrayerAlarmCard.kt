package com.ibadatplus.app.ui.alarm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadatplus.app.data.model.AlarmToneType
import com.ibadatplus.app.data.model.PrayerAlarmConfig
import com.ibadatplus.app.data.model.PrayerName

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrayerAlarmCard(
    config: PrayerAlarmConfig,
    nextScheduledTime: String,
    onToggle: (Boolean) -> Unit,
    onToneChanged: (AlarmToneType) -> Unit,
    onMinutesBeforeChanged: (Int) -> Unit,
    onTestAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = config.isEnabled

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) Color.White else Color(0xFFF9FAFB)
        ),
        border = BorderStroke(
            1.dp,
            if (isEnabled) Color(0xFFD1FAE5) else Color(0xFFE5E7EB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEnabled) 3.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon, Prayer Name, Arabic Name, Next Alarm & Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (isEnabled) Color(0xFF10B981) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = config.prayerName.displayName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEnabled) Color(0xFF111827) else Color(0xFF6B7280)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = config.prayerName.arabicName,
                                fontSize = 14.sp,
                                color = if (isEnabled) Color(0xFF10B981) else Color(0xFF9CA3AF)
                            )
                        }

                        Text(
                            text = if (isEnabled) nextScheduledTime else "Alarm is Turned Off",
                            fontSize = 12.sp,
                            color = if (isEnabled) Color(0xFF047857) else Color(0xFF9CA3AF),
                            fontWeight = if (isEnabled) FontWeight.SemiBold else FontWeight.Normal
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
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color(0xFFF3F4F6))
                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Alert Type Choice (Azan, Normal Chime, Vibration)
                    Text(
                        text = "ALERT TYPE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AlarmToneType.values().forEach { tone ->
                            val isSelected = tone == config.toneType
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFECFDF5) else Color(0xFFF3F4F6),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF10B981) else Color.Transparent
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onToneChanged(tone) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (tone) {
                                            AlarmToneType.AZAN -> Icons.Default.GraphicEq
                                            AlarmToneType.NORMAL_BEEP -> Icons.Default.Notifications
                                            AlarmToneType.VIBRATION_ONLY -> Icons.Default.Vibration
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF047857) else Color(0xFF4B5563),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tone.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF047857) else Color(0xFF374151)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Early Reminder Offset Choice (Exact time, 5m, 10m, 15m pehle)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "REMINDER TIME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp
                            )
                        }

                        // Test alarm preview button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onTestAlarm() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Test Alarm",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val offsetOptions = listOf(0, 5, 10, 15, 30)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        offsetOptions.forEach { minutes ->
                            val isSelected = minutes == config.minutesBefore
                            val label = when (minutes) {
                                0 -> "Exact Time"
                                else -> "$minutes min pehle"
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF10B981) else Color(0xFFF3F4F6),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onMinutesBeforeChanged(minutes) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF374151),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
