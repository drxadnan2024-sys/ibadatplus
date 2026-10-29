package com.ibadatplus.app.ui.prayer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadatplus.app.data.model.PrayerItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun JamatTimePickerDialog(
    prayerItem: PrayerItem,
    defaultOffsetMinutes: Int,
    onSaveJamatTime: (LocalTime) -> Unit,
    onResetToDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    val initialTime = prayerItem.jamatTime ?: prayerItem.time.plusMinutes(defaultOffsetMinutes.toLong())

    var selectedHour24 by remember { mutableIntStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableIntStateOf(initialTime.minute) }

    // Helpers to convert to 12-hour format for display
    val isPm = selectedHour24 >= 12
    val hour12 = when (val h = selectedHour24 % 12) {
        0 -> 12
        else -> h
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Masjid Jamat Time",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F5132)
                    )
                    Text(
                        text = "${prayerItem.name.displayName} (${prayerItem.name.arabicName})",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Azaan time banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF3F4F6),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Azaan / Start Time:",
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563)
                        )
                        Text(
                            text = prayerItem.formatted12Hour,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1F2937)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Digital Jamat Time Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Hours Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                selectedHour24 = (selectedHour24 + 1) % 24
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Hour", tint = Color(0xFF10B981))
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.size(width = 62.dp, height = 54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = String.format(Locale.ENGLISH, "%02d", hour12),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F5132)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                selectedHour24 = if (selectedHour24 - 1 < 0) 23 else selectedHour24 - 1
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Sub Hour", tint = Color(0xFF10B981))
                        }
                    }

                    Text(
                        text = ":",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F5132),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 20.dp)
                    )

                    // Minutes Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                selectedMinute = (selectedMinute + 1) % 60
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Min", tint = Color(0xFF10B981))
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.size(width = 62.dp, height = 54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = String.format(Locale.ENGLISH, "%02d", selectedMinute),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F5132)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                selectedMinute = if (selectedMinute - 1 < 0) 59 else selectedMinute - 1
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Sub Min", tint = Color(0xFF10B981))
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // AM / PM Toggle
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 18.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (!isPm) Color(0xFF10B981) else Color(0xFFE5E7EB),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    if (isPm) selectedHour24 = (selectedHour24 - 12).coerceAtLeast(0)
                                }
                        ) {
                            Text(
                                text = "AM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isPm) Color.White else Color(0xFF4B5563),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isPm) Color(0xFF10B981) else Color(0xFFE5E7EB),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    if (!isPm) selectedHour24 = (selectedHour24 + 12) % 24
                                }
                        ) {
                            Text(
                                text = "PM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPm) Color.White else Color(0xFF4B5563),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Offset Chips relative to Azaan
                Text(
                    text = "Quick Adjust from Azaan:",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val offsets = listOf(10, 15, 20, 25, 30)
                    offsets.forEach { offset ->
                        val target = prayerItem.time.plusMinutes(offset.toLong())
                        val isCurrentSelected = target.hour == selectedHour24 && target.minute == selectedMinute

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrentSelected) Color(0xFF10B981) else Color(0xFFF3F4F6),
                            border = BorderStroke(
                                1.dp,
                                if (isCurrentSelected) Color(0xFF10B981) else Color(0xFFE5E7EB)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedHour24 = target.hour
                                    selectedMinute = target.minute
                                }
                        ) {
                            Text(
                                text = "+$offset",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrentSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrentSelected) Color.White else Color(0xFF374151),
                                modifier = Modifier.padding(vertical = 5.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                if (prayerItem.isCustomJamat) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = {
                            onResetToDefault()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset to Default (+${defaultOffsetMinutes}m)",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pickedTime = LocalTime.of(selectedHour24, selectedMinute)
                    onSaveJamatTime(pickedTime)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Jamat Time", fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF6B7280), fontSize = 13.sp)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
