package com.courtquestpro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CourtOnboardingWelcome() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(
            Modifier.fillMaxWidth()
                .aspectRatio(342f / 178f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(CourtQuestDesign.blueprintShadow, CourtQuestDesign.warmSurface)
                    )
                )
        ) {
            Image(
                painter = painterResource(R.drawable.courtquest_half_court),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                Modifier.align(Alignment.CenterStart).padding(start = 18.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    "PLAYER LAB",
                    color = CourtQuestDesign.accent,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                Text(
                    "TRAINING",
                    color = CourtQuestDesign.textSecondary,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.9.sp
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(CourtQuestDesign.spacingMd)) {
            Text(
                "Build your\ngame.",
                modifier = Modifier.semantics { heading() },
                color = CourtQuestDesign.textPrimary,
                fontSize = 46.sp,
                lineHeight = 47.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.4).sp
            )
            Text(
                "Choose your level, focus, and training days to shape your daily drills.",
                color = CourtQuestDesign.textSecondary,
                fontSize = 16.sp,
                lineHeight = 23.sp
            )
        }
    }
}
