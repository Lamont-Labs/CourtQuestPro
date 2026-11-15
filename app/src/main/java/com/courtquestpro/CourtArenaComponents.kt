package com.courtquestpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Arena artwork remains decorative; the tint and court-light falloff are drawn natively. */
@Composable
internal fun CourtArenaHero(
    resourceId: Int,
    modifier: Modifier = Modifier,
    imageAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier.clip(RoundedCornerShape(26.dp))
            .background(CourtQuestDesign.surface)
            .border(1.dp, Color.White.copy(alpha = .14f), RoundedCornerShape(26.dp))
    ) {
        Image(
            painter = painterResource(resourceId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = imageAlignment
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0x18000000),
                    .46f to Color(0x3C080503),
                    1f to Color(0xF2050505)
                )
            )
        )
        Box(Modifier.fillMaxSize().background(
            Brush.horizontalGradient(listOf(Color(0x284B1200), Color.Transparent, Color(0x18430F00)))
        ))
        content()
    }
}

/** The original jersey atlas shows the established display fallback without mutating a blank profile. */
@Composable
internal fun CourtAthleteIdentity(save: SaveState, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            Modifier.size(92.dp).shadow(14.dp, CircleShape, ambientColor = CourtQuestDesign.accentVivid.copy(alpha = .2f))
                .clip(CircleShape).background(Color(0xFF171719))
                .border(1.dp, CourtQuestDesign.accent.copy(alpha = .45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CourtQuestJerseyArtwork(save.jerseyNumber ?: 7, Modifier.fillMaxSize().padding(5.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                save.playerName.ifBlank { "Your player profile" },
                color = CourtQuestDesign.textPrimary,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                if (save.jerseyNumber == null) "JERSEY NOT SET · DISPLAY 7"
                else "JERSEY #${displayedJerseyNumber(save.jerseyNumber)}",
                color = CourtQuestDesign.accent,
                style = MaterialTheme.typography.labelLarge
            )
            Text("${save.difficulty} · ${rankFor(save.totalXp)} · ${save.totalXp} XP",
                color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
            CourtRankMeter(save.totalXp)
        }
    }
}

/** Compact dimensional icon treatment for native route and skill controls. */
@Composable
internal fun CourtSportIcon(icon: ImageVector, modifier: Modifier = Modifier, description: String? = null) {
    Box(
        modifier.size(48.dp).shadow(10.dp, RoundedCornerShape(15.dp),
            ambientColor = CourtQuestDesign.accentVivid.copy(alpha = .16f))
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF302018), Color(0xFF141416))))
            .border(1.dp, CourtQuestDesign.accent.copy(alpha = .35f), RoundedCornerShape(15.dp))
            .semantics { if (description != null) contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = CourtQuestDesign.accent, modifier = Modifier.size(24.dp))
    }
}

/** Native shield mark, intentionally an icon rather than a claimed earned badge. */
@Composable
internal fun CourtQuestShield(modifier: Modifier = Modifier, progress: Float = 1f) {
    Canvas(modifier.size(54.dp)) {
        val path = Path().apply {
            moveTo(size.width * .5f, size.height * .06f)
            lineTo(size.width * .9f, size.height * .2f)
            lineTo(size.width * .84f, size.height * .62f)
            quadraticTo(size.width * .75f, size.height * .85f, size.width * .5f, size.height * .96f)
            quadraticTo(size.width * .25f, size.height * .85f, size.width * .16f, size.height * .62f)
            lineTo(size.width * .1f, size.height * .2f)
            close()
        }
        drawPath(path, Brush.linearGradient(listOf(Color(0xFFFF8A32), Color(0xFF9D3209))))
        drawPath(path, Color.White.copy(alpha = .48f), style = Stroke(width = 1.5.dp.toPx()))
        val p = progress.coerceIn(0f, 1f)
        drawArc(Color(0xFFFFD2A8), -90f, p * 360f, false,
            style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
internal fun CourtArenaTitle(eyebrow: String, title: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(eyebrow, color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelSmall)
        Text(title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineLarge)
    }
}
