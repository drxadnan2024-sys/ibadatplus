package com.ibadatplus.app.tasbeeh.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadatplus.app.tasbeeh.model.DhikrItem

@Composable
fun DhikrSelectionChips(
    allDhikrs: List<DhikrItem>,
    selectedDhikr: DhikrItem,
    onDhikrSelected: (DhikrItem) -> Unit,
    onAddCustomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        allDhikrs.forEach { dhikr ->
            val isSelected = dhikr.id == selectedDhikr.id

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF198754) else Color.White,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF198754) else Color(0xFFE5E7EB)
                ),
                shadowElevation = if (isSelected) 3.dp else 1.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDhikrSelected(dhikr) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = dhikr.nameEnglish,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color(0xFF1F2937)
                        )
                        Text(
                            text = dhikr.nameArabic,
                            fontSize = 11.sp,
                            color = if (isSelected) Color(0xFFD1FAE5) else Color(0xFF6B7280)
                        )
                    }

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // "+ Custom Dhikr" chip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF3F4F6),
            border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAddCustomClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Custom",
                    tint = Color(0xFF0F5132),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Custom Dhikr",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F5132)
                )
            }
        }
    }
}
