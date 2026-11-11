package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AppHeader(title: String, subtitle: String = "") {
    Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 20.dp)) {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.SportsBasketball, contentDescription = null,
                    tint = CourtQuestDesign.iconForeground,
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(9.dp))
                        .background(CourtQuestDesign.accentVivid).padding(5.dp))
                Text("COURTQUEST", color = CourtQuestDesign.textPrimary,
                    style = MaterialTheme.typography.labelMedium)
            }
            Box(Modifier.heightIn(min = 28.dp), contentAlignment = Alignment.CenterStart) {
                CourtEyebrow("PLAYER LAB")
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.displaySmall)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
