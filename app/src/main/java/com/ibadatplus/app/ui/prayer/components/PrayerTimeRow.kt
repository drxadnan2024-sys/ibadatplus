package com.ibadatplus.app.ui.prayer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadatplus.app.data.model.BellAlarmState
import com.ibadatplus.app.data.model.PrayerItem
import com.ibadatplus.app.data.model.PrayerName

@Composable
fun PrayerTimeRow(
    prayerItem: PrayerItem,
    bellState: BellAlarmState = BellAlarmState.FULL_AZAN,
    onBellClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isNext = prayerItem.isNext
    val isPassed = prayerItem.isPassed && !isNext

    val containerColor = when {
        isNext -> Color(0xFFE8F5E9) // Light fresh emerald green
        isPassed -> Color(0xFFF8F9FA) // Subtle grayish for passed prayers
        else -> Color.White
    }

    val contentColor = when {
        isNext -> Color(0xFF0F5132)
        isPassed -> Color(0xFF6C757D)
        else -> Color(0xFF212529)
    }

    val border = if (isNext) {
        BorderStroke(1.5.dp, Color(0xFF198754))
    } else {
        BorderStroke(1.dp, Color(0xFFE9ECEF))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon & English/Arabic Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isNext) Color(0xFF198754) else Color(0xFFEDF2F7),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getPrayerIcon(prayerItem.name),
                            contentDescription = prayerItem.name.displayName,
                            tint = if (isNext) Color.White else Color(0xFF495057),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayerItem.name.displayName,
                            fontSize = 15.sp,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                            color = contentColor
                        )

                        if (isNext) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF198754),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "NEXT",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = prayerItem.name.arabicName,
                        fontSize = 12.sp,
                        color = if (isNext) Color(0xFF198754) else Color(0xFF868E96)
                    )
                }
            }

            // 12-Hour Formatted Time & 3-State Bell Action
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prayerItem.formatted12Hour,
                    fontSize = 16.sp,
                    fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isNext) Color(0xFF0F5132) else contentColor
                )

                // 3-State Bell icon (Full Azan, Beep/Notification, Silent/Off)
                if (onBellClick != null && prayerItem.name != PrayerName.SUNRISE) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = CircleShape,
                        color = when (bellState) {
                            BellAlarmState.FULL_AZAN -> Color(0xFFE8F5E9)
                            BellAlarmState.BEEP_NOTIFICATION -> Color(0xFFEFF6FF)
                            BellAlarmState.SILENT_OFF -> Color(0xFFF3F4F6)
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { onBellClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (bellState) {
                                    BellAlarmState.FULL_AZAN -> Icons.Default.NotificationsActive
                                    BellAlarmState.BEEP_NOTIFICATION -> Icons.Default.Notifications
                                    BellAlarmState.SILENT_OFF -> Icons.Default.NotificationsOff
                                },
                                contentDescription = "Alarm State: ${bellState.displayName}",
                                tint = when (bellState) {
                                    BellAlarmState.FULL_AZAN -> Color(0xFF10B981)
                                    BellAlarmState.BEEP_NOTIFICATION -> Color(0xFF2563EB)
                                    BellAlarmState.SILENT_OFF -> Color(0xFF9CA3AF)
                                },
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                } else if (isPassed) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Passed",
                        tint = Color(0xFFADB5BD),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun getPrayerIcon(prayerName: PrayerName): ImageVector {
    return when (prayerName) {
        PrayerName.FAJR -> Icons.Default.Brightness2
        PrayerName.SUNRISE -> Icons.Default.WbTwilight
        PrayerName.DHUHR -> Icons.Default.WbSunny
        PrayerName.ASR -> Icons.Default.Brightness7
        PrayerName.MAGHRIB -> Icons.Default.Brightness6
        PrayerName.ISHA -> Icons.Default.Brightness5
    }
}
