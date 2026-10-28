package com.ibadatplus.app.ui.alarm.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadatplus.app.alarm.PrayerAlarmScheduler
import com.ibadatplus.app.data.model.OemBatteryOptimizationGuide

@Composable
fun BatteryOptimizationGuideCard(
    isIgnoringOptimization: Boolean,
    onOptimizeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    val guides = listOf(
        OemBatteryOptimizationGuide(
            brand = "Xiaomi / Redmi / POCO (MIUI / HyperOS)",
            steps = "1. Settings > Apps > Manage Apps > Ibadat Plus.\n2. Enable 'Autostart'.\n3. Battery Saver > Select 'No restrictions'."
        ),
        OemBatteryOptimizationGuide(
            brand = "Samsung (One UI)",
            steps = "1. Settings > Battery > Background usage limits.\n2. 'Never sleeping apps' me Ibadat Plus ko add karein."
        ),
        OemBatteryOptimizationGuide(
            brand = "OnePlus / Realme / Oppo (ColorOS / OxygenOS)",
            steps = "1. Settings > Battery > App Battery Management > Ibadat Plus.\n2. 'Allow Background Activity' aur 'Allow auto-launch' on karein."
        ),
        OemBatteryOptimizationGuide(
            brand = "Vivo / iQOO (Funtouch OS)",
            steps = "1. Settings > Battery > High background power consumption > Ibadat Plus allow karein."
        ),
        OemBatteryOptimizationGuide(
            brand = "Stock Android / Google Pixel / Motorola",
            steps = "1. App info > App battery usage > 'Unrestricted' select karein."
        )
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isIgnoringOptimization) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
        ),
        border = BorderStroke(
            1.dp,
            if (isIgnoringOptimization) Color(0xFF86EFAC) else Color(0xFFFDE68A)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isIgnoringOptimization) Icons.Default.CheckCircle else Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = if (isIgnoringOptimization) Color(0xFF16A34A) else Color(0xFFD97706),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isIgnoringOptimization) "Battery Optimization: Unrestricted" else "Battery Optimization Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isIgnoringOptimization) Color(0xFF14532D) else Color(0xFF78350F)
                        )
                        Text(
                            text = if (isIgnoringOptimization) "Alarms will ring reliably on time." else "Kuch phones me background battery killer alarm band kar deta hai.",
                            fontSize = 11.sp,
                            color = if (isIgnoringOptimization) Color(0xFF15803D) else Color(0xFF92400E)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand guide",
                        tint = if (isIgnoringOptimization) Color(0xFF16A34A) else Color(0xFFD97706)
                    )
                }
            }

            if (!isIgnoringOptimization) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        try {
                            val intent = PrayerAlarmScheduler.createBatteryOptimizationIntent(context)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            onOptimizeClicked()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Disable Battery Optimization (Allow Alarms)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Divider(color = Color(0x33000000))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📱 Phone Specific Settings Guide:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    guides.forEach { guide ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                text = guide.brand,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF374151)
                            )
                            Text(
                                text = guide.steps,
                                fontSize = 11.sp,
                                color = Color(0xFF4B5563),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
