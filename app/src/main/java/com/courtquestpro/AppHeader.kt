package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AppHeader(title: String, subtitle: String = "") {
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(
                CourtQuestDesign.warmSurface, CourtQuestDesign.elevatedSurface, CourtQuestDesign.background
            )))
            .padding(start = 24.dp, end = 24.dp, top = 30.dp, bottom = 34.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(listOf(CourtQuestDesign.iconGradientStart, CourtQuestDesign.accentVivid))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SportsBasketball, null,
                    tint = CourtQuestDesign.iconForeground, modifier = Modifier.size(27.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("COURTQUEST", color = CourtQuestDesign.textPrimary, fontSize = 14.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 2.1.sp)
                Text("PERFORMANCE STUDIO", color = CourtQuestDesign.accent, fontSize = 9.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.clip(RoundedCornerShape(50))
                    .background(CourtQuestDesign.chipBackground)
                    .border(1.dp, CourtQuestDesign.chipBorder, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text("PLAYER LAB", color = CourtQuestDesign.textPrimary, fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 0.7.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(28.dp))
        Box(Modifier.width(56.dp).height(3.dp).clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(CourtQuestDesign.accent, CourtQuestDesign.accentVivid))))
        Spacer(Modifier.height(13.dp))
        Text(title, color = CourtQuestDesign.textPrimary, fontSize = 32.sp,
            fontWeight = FontWeight.Black, lineHeight = 38.sp,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(11.dp))
            Text(subtitle, color = CourtQuestDesign.textSecondary, fontSize = 12.sp,
                fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp, lineHeight = 18.sp,
                maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

