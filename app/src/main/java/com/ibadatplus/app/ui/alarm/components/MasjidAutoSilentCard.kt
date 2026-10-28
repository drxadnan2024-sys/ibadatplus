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
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Mosque
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

@Composable
fun MasjidAutoSilentCard(
    config: AutoSilentConfig,
    isDndGranted: Boolean,
    onToggle: (Boolean) -> Unit,
    onDurationChanged: (Int) -> Unit,
    onModeChanged: (AutoSilentMode) -> Unit,
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
                            text = if (isEnabled) "Namaz ke dauran phone silent rahega (${config.durationMinutes} min)" else "Namaz me phone automatically silent karein",
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
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Duration Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SILENT DURATION (RESTORES AUTOMATICALLY):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val durations = listOf(15, 20, 25, 30)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                    text = "$minutes min",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF374151),
                                    modifier = Modifier.padding(vertical = 7.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Mode Selector: Vibrate vs DND Silent
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
                                Text(
                                    text = "Vibrate Only",
                                    fontSize = 11.sp,
                                    fontWeight = if (isVibrate) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isVibrate) Color(0xFF047857) else Color(0xFF374151)
                                )
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
                                Text(
                                    text = "Pure Silent (DND)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isDnd) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isDnd) Color(0xFF047857) else Color(0xFF374151)
                                )
                            }
                        }
                    }

                    // DND Permission prompt if DND mode selected without permission
                    if (config.silentMode == AutoSilentMode.DND_SILENT && !isDndGranted) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "DND Permission Required",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Do Not Disturb mode toggle karne ke liye Notification Policy Access allow karein.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        try {
                                            context.startActivity(AutoSilentManager.createDndSettingsIntent())
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Grant DND Access", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
