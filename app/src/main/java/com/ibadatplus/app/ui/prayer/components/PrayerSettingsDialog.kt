package com.ibadatplus.app.ui.prayer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadatplus.app.data.model.AsrMadhab
import com.ibadatplus.app.data.model.CalculationMethod

@Composable
fun PrayerSettingsDialog(
    currentMethod: CalculationMethod,
    currentMadhab: AsrMadhab,
    onMethodSelected: (CalculationMethod) -> Unit,
    onMadhabSelected: (AsrMadhab) -> Unit,
    onDismissRequest: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF198754)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calculation Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Section: Calculation Method
                Text(
                    text = "CALCULATION METHOD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF198754),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                CalculationMethod.values().forEach { method ->
                    val isSelected = method == currentMethod
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMethodSelected(method) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onMethodSelected(method) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF198754))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = method.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = method.description,
                                fontSize = 11.sp,
                                color = Color(0xFF6C757D)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFE9ECEF))
                Spacer(modifier = Modifier.height(16.dp))

                // Section: Asr Juristic Method
                Text(
                    text = "ASR JURISTIC METHOD (MADHAB)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF198754),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                AsrMadhab.values().forEach { madhab ->
                    val isSelected = madhab == currentMadhab
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMadhabSelected(madhab) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onMadhabSelected(madhab) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF198754))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = madhab.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (madhab == AsrMadhab.HANAFI) "Shadow length = 2x object length" else "Shadow length = 1x object length",
                                fontSize = 11.sp,
                                color = Color(0xFF6C757D)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Done", color = Color(0xFF198754), fontWeight = FontWeight.Bold)
            }
        }
    )
}
